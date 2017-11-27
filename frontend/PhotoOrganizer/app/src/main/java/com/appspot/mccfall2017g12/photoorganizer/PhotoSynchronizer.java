package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executor;

public class PhotoSynchronizer {

    //TODO hack, must be changed!
    public volatile static boolean isListening = false;

    //TODO Should be calculated based on network state & settings
    private final static String CURRENT_RESOLUTION_LEVEL = ResolutionTools.LEVEL_FULL;

    private final String groupId;
    private final GalleryDatabase database;
    private final Handler mainHandler;
    private final Executor executor;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseStorage firebaseStorage;

    public PhotoSynchronizer(String groupId, Context context) {
        GalleryDatabase.initialize(context);

        this.groupId = groupId;
        this.database = GalleryDatabase.getInstance();
        this.executor = ThreadTools.EXECUTOR;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public void listen() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                database.photoSyncDao().releaseAll();
                firebaseDatabase.getReference("photos").child(groupId)
                        .addChildEventListener(new PhotoEventListener());
            }
        });
    }

    // This should be called when network/settings change.
    @WorkerThread
    private void downloadAllImprovablePhotos() {
        final String[] photoIds = database.galleryDao().loadPhotosWithHigherOnlineResolution(
                groupId, ResolutionTools.getResolution(CURRENT_RESOLUTION_LEVEL));

        for (final String photoId : photoIds) {
            if (shouldDownload(photoId)) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        new PhotoDownload(photoId).start();
                    }
                });
            }
        }
    }

    @WorkerThread
    private boolean shouldDownload(String photoId) {
        Photo.ResolutionInfo resolution = database.galleryDao().loadPhotoResolution(photoId);

        if (resolution == null)
            return false;

        int sourceResolution = ResolutionTools.getResolution(CURRENT_RESOLUTION_LEVEL,
                resolution.online);

        return sourceResolution > resolution.local;
    }

    @WorkerThread
    private boolean shouldUpload(String photoId) {
        Photo.ResolutionInfo resolution = database.galleryDao().loadPhotoResolution(photoId);

        if (resolution == null)
            return false;

        int sourceResolution = ResolutionTools.getResolution(CURRENT_RESOLUTION_LEVEL,
                resolution.local);

        return sourceResolution > resolution.online;
    }

    private class PhotoEventListener extends AsyncChildEventListener {

        public PhotoEventListener() {
            super(PhotoSynchronizer.this.executor);
        }

        @Override
        @WorkerThread
        public void onChildAddedAsync(final DataSnapshot dataSnapshot, String previousChildName) {
            String photoId = dataSnapshot.getKey();

            if (!database.galleryDao().photoExists(photoId)) {
                Photo photo = new Photo();
                photo.photoId = photoId;
                photo.albumId = groupId;
                photo.resolution.local = -1;
                database.galleryDao().insertPhotos(photo);
                fetchAndSetAuthorNameForPhotoAsync(dataSnapshot);
            }

            updatePhoto(dataSnapshot);
        }

        @Override
        @WorkerThread
        public void onChildChangedAsync(final DataSnapshot dataSnapshot, String previousChildName) {
            updatePhoto(dataSnapshot);
        }

        @Override
        public void onChildRemovedAsync(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMovedAsync(DataSnapshot dataSnapshot, String previousChildName) {

        }

        @Override
        public void onCancelledAsync(DatabaseError databaseError) {

        }

        private void fetchAndSetAuthorNameForPhotoAsync(final DataSnapshot dataSnapshot) {
            String photoId = dataSnapshot.getKey();
            String userId = dataSnapshot.child("author").getValue(String.class);

            DatabaseReference usersRef = firebaseDatabase.getReference("users");

            usersRef.child(userId).child("username").addListenerForSingleValueEvent(
                    new UserNameEventListener(photoId));
        }

        @WorkerThread
        private void updatePhoto(final DataSnapshot dataSnapshot) {
            final String photoId = dataSnapshot.getKey();

            if (dataSnapshot.hasChild("resolution")) {
                int onlineResolution = dataSnapshot.child("resolution").getValue(int.class);
                database.galleryDao().improvePhotoOnlineResolution(photoId, onlineResolution);
            }
            if (dataSnapshot.hasChild("files/" + CURRENT_RESOLUTION_LEVEL)
                    && shouldDownload(photoId)) {

                mainHandler.post(new Runnable() {
                    @Override
                    @MainThread
                    public void run() {
                        new PhotoDownload(photoId).start();
                    }
                });
            }
            if (dataSnapshot.hasChild("people")) {
                final int people = dataSnapshot.child("people").getValue(int.class);
                database.galleryDao().updatePhotoPeople(photoId, people);
            }
        }

        private class UserNameEventListener extends AsyncValueEventListener {

            private final String photoId;

            public UserNameEventListener(String photoId) {
                super(executor);

                this.photoId = photoId;
            }

            @Override
            @WorkerThread
            public void onDataChangeAsync(final DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.getValue(String.class);
                database.galleryDao().updatePhotoAuthor(photoId, userName);
            }

            @Override
            public void onCancelledAsync(DatabaseError databaseError) {

            }
        }
    }

    public abstract class PhotoSync {
        protected final String photoId;

        public PhotoSync(String photoId) {
            this.photoId = photoId;
        }

        @MainThread
        public void start() {
            final LiveData<PhotoSyncLock> observable = database.photoSyncDao().get(photoId);

            observable.observeForever(new AsyncObserver<PhotoSyncLock>(executor, mainHandler) {
                @Override
                @WorkerThread
                protected void onChangedAsync(@Nullable PhotoSyncLock photoSyncLock) {
                    if (photoSyncLock != null) return;

                    if (database.photoSyncDao().tryStart(photoId)) {
                        removeFrom(observable);
                        run();
                    }
                }
            });
        }

        @WorkerThread
        protected void release() {
            database.photoSyncDao().release(new PhotoSyncLock(photoId));
        }

        @WorkerThread
        protected abstract void run();
    }

    public void uploadPhoto(final String photoId) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                new PhotoUpload(photoId).start();
            }
        });
    }

    public class PhotoUpload extends PhotoSync {

        private volatile Photo.FileInfo volatileFileInfo;

        public PhotoUpload(String photoId) {
            super(photoId);
        }

        @Override
        protected void run() {
            if (!shouldUpload(photoId)) {
                release();
                return;
            }

            Photo.FileInfo localFileInfo = database.galleryDao().loadPhotoFileInfo(photoId);

            if (localFileInfo == null) {
                release();
                return;
            }

            // if CURRENT_RESOLUTION_LEVEL = "low" or "high" shrink photo

            Photo.FileInfo onlineFileInfo = new Photo.FileInfo();
            onlineFileInfo.file = UUID.randomUUID().toString() + ".jpg";
            onlineFileInfo.resolution = localFileInfo.resolution;

            volatileFileInfo = onlineFileInfo;

            File localFile = FileTools.get(localFileInfo.file);

            StorageReference imagesRef = firebaseStorage.getReference("images");
            StorageReference fileRef = imagesRef.child(groupId).child(onlineFileInfo.file);

            FileUploadListener listener = new FileUploadListener();

            fileRef.putFile(Uri.fromFile(localFile))
                    .addOnSuccessListener(executor, listener)
                    .addOnFailureListener(executor, listener);

            release();
        }

        private class FileUploadListener implements OnFailureListener,
                OnSuccessListener<UploadTask.TaskSnapshot> {

            @Override
            public void onFailure(@NonNull Exception e) {
                release();
            }

            @Override
            public void onSuccess(UploadTask.TaskSnapshot result) {
                DatabaseReference photosRef = firebaseDatabase.getReference(
                        "photos").child(groupId).child(photoId).child("files/full");

                SetFilenameListener listener = new SetFilenameListener();
                photosRef.setValue(volatileFileInfo.file)
                        .addOnSuccessListener(executor, listener)
                        .addOnFailureListener(executor, listener);
            }
        }

        private class SetFilenameListener implements OnFailureListener, OnSuccessListener<Void> {

            @Override
            public void onFailure(@NonNull Exception e) {
                release();
            }

            @Override
            public void onSuccess(Void aVoid) {
                database.galleryDao().improvePhotoOnlineResolution(photoId,
                        volatileFileInfo.resolution);
                release();
            }
        }
    }

    public class PhotoDownload extends PhotoSync {

        private volatile String volatileFilename;

        public PhotoDownload(String photoId) {
            super(photoId);
        }

        @Override
        @WorkerThread
        protected void run() {
            if (!shouldDownload(photoId)) {
                release();
                return;
            }

            DatabaseReference photosRef = firebaseDatabase.getReference("photos");
            photosRef.child(groupId).child(photoId).child("files").child(CURRENT_RESOLUTION_LEVEL)
                    .addListenerForSingleValueEvent(new GetFilenameListener());
        }

        private class GetFilenameListener extends AsyncValueEventListener {

            public GetFilenameListener() {
                super(executor);
            }

            @Override
            @WorkerThread
            public void onDataChangeAsync(DataSnapshot dataSnapshot) {
                String filename = dataSnapshot.getValue(String.class);

                if (filename == null) {
                    release();
                    return;
                }

                volatileFilename = filename;

                StorageReference imagesRef = firebaseStorage.getReference("images");
                StorageReference fileRef = imagesRef.child(groupId).child(filename);

                FileDownloadListener listener = new FileDownloadListener();

                File localFile = FileTools.get(filename);
                fileRef.getFile(localFile)
                        .addOnSuccessListener(executor, listener)
                        .addOnFailureListener(executor, listener);
            }

            @Override
            @WorkerThread
            public void onCancelledAsync(DatabaseError databaseError) {
                release();
            }
        }

        private class FileDownloadListener implements OnFailureListener,
                OnSuccessListener<FileDownloadTask.TaskSnapshot>  {

            @Override
            @WorkerThread
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                String filename = volatileFilename;

                File localFile = FileTools.get(filename);
                int resolution = ResolutionTools.calculateResolution(
                        localFile.getAbsolutePath());

                String fileToBeRemoved = database.galleryDao()
                        .tryUpdatePhotoFile(photoId, filename, resolution);

                if (fileToBeRemoved != null)
                    FileTools.get(fileToBeRemoved).delete();

                release();
            }

            @Override
            @WorkerThread
            public void onFailure(@NonNull Exception e) {
                release();
            }
        }
    }
}
