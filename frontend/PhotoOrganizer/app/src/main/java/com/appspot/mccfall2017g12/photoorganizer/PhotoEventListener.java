package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.content.Context;
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

import java.io.File;
import java.util.concurrent.Executors;

public class PhotoEventListener extends AsyncChildEventListener {

    private final String groupId;
    private final GalleryDatabase database;
    private final Handler mainHandler;

    //TODO hack, must be changed!
    public volatile static boolean isListening = false;

    //TODO Should be calculated based on network state & settings
    private final static String CURRENT_RESOLUTION_LEVEL = ResolutionTools.LEVEL_HIGH;

    public PhotoEventListener(String groupId, Context context) {
        super(Executors.newCachedThreadPool());
        this.groupId = groupId;

        GalleryDatabase.initialize(context);
        this.database = GalleryDatabase.getInstance();

        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    @WorkerThread
    public void onChildAddedAsync(final DataSnapshot dataSnapshot, String previousChildName) {
        String photoId = dataSnapshot.getKey();

        Photo photo = database.galleryDao().loadPhoto(photoId);

        if (photo == null) {
            photo = new Photo();
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

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(userId).child("name").addListenerForSingleValueEvent(
                new UserNameEventListener(photoId));
    }

    @WorkerThread
    private void updatePhoto(final DataSnapshot dataSnapshot) {
        final String photoId = dataSnapshot.getKey();

        if (dataSnapshot.hasChild("resolution")) {
            int onlineResolution = dataSnapshot.child("resolution").getValue(int.class);
            database.galleryDao().updatePhotoOnlineResolution(photoId, onlineResolution);
        }
        if (dataSnapshot.hasChild("files/" + CURRENT_RESOLUTION_LEVEL)
                && shouldDownload(photoId)) {

            mainHandler.post(new Runnable() {
                @Override
                @MainThread
                public void run() {
                    downloadPhotoFileIfNecessary(photoId);
                }
            });
        }
        if (dataSnapshot.hasChild("people")) {
            final int people = dataSnapshot.child("people").getValue(int.class);
            database.galleryDao().updatePhotoPeople(photoId, people);
        }
    }

    @WorkerThread
    private boolean shouldDownload(String photoId) {
        Photo.ResolutionInfo resolution = database.galleryDao().loadPhotoResolution(photoId);

        if (resolution == null)
            return false;
        int targetResolution = ResolutionTools.getResolution(CURRENT_RESOLUTION_LEVEL,
                resolution.online);
        return targetResolution > resolution.local;
    }

    // This method will be moved and made public because it should be called for each photo
    // (in the syncing album) whose online resolution is higher than its local resolution
    // whenever settings or network changes.
    @MainThread
    private void downloadPhotoFileIfNecessary(final String photoId) {

        final LiveData<DownloadLock> observable = database.galleryDao().getDownloadLock(photoId);
        observable.observeForever(new AsyncObserver<DownloadLock>(executor, mainHandler) {
            @Override
            @WorkerThread
            protected void onChangedAsync(@Nullable DownloadLock downloadLock) {
                if (downloadLock != null) return;

                if (database.galleryDao().tryStartDownload(photoId)) {
                    removeFrom(observable);
                    downloadPhotoFileIfNecessaryInternal(photoId);
                }
            }
        });
    }

    // The caller must have set isDownloading = true for the photo!
    @WorkerThread
    private void releaseDownload(String photoId) {
        database.galleryDao().releaseDownload(new DownloadLock(photoId));
    }

    // The caller must have set isDownloading = true for the photo!
    @WorkerThread
    private void downloadPhotoFileIfNecessaryInternal(String photoId) {
        if (!shouldDownload(photoId)) {
            releaseDownload(photoId);
            return;
        }

        DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference("photos");
        photosRef.child(groupId).child(photoId).child("files").child(CURRENT_RESOLUTION_LEVEL)
                .addListenerForSingleValueEvent(new PhotoFileListener(photoId));
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

    private class PhotoFileListener extends AsyncValueEventListener
            implements OnSuccessListener<FileDownloadTask.TaskSnapshot>, OnFailureListener {

        private final String photoId;
        private volatile String volatileFilename;

        public PhotoFileListener(String photoId) {
            super(executor);
            this.photoId = photoId;
        }

        @Override
        @WorkerThread
        public void onDataChangeAsync(final DataSnapshot dataSnapshot) {
            String filename = dataSnapshot.getValue(String.class);

            if (filename == null) {
                releaseDownload(photoId);
                return;
            }

            volatileFilename = filename;

            StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images");
            StorageReference fileRef = imagesRef.child(groupId).child(filename);

            File localFile = FileTools.get(filename);
            fileRef.getFile(localFile).addOnSuccessListener(executor, this)
                    .addOnFailureListener(executor, this);
        }

        @Override
        @WorkerThread
        public void onCancelledAsync(DatabaseError databaseError) {
            releaseDownload(photoId);
        }

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

            releaseDownload(photoId);
        }

        @Override
        @WorkerThread
        public void onFailure(@NonNull Exception e) {
            releaseDownload(photoId);
        }
    }
}
