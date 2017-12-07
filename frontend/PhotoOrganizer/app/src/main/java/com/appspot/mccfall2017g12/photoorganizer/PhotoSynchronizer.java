package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Transaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executor;

public class PhotoSynchronizer {

    private static final int SCHEDULE_PERIOD = 30*1000; // 30 seconds

    //TODO Should be calculated based on network state & settings
    //private final static String CURRENT_RESOLUTION_LEVEL = ResolutionTools.LEVEL_FULL;
    private  String CURRENT_RESOLUTION_LEVEL;
    private final String groupId;
    private final LocalDatabase database;
    private final PhotoSyncDatabase photoSyncDb;
    private final Handler mainHandler;
    private final Executor executor;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseStorage firebaseStorage;
    private final PhotoEventListener photoEventListener;
    private final DatabaseReference groupReference;
    private final Context context;
    private final Notifier notifier;
    private final Timer timer;

    public PhotoSynchronizer(String groupId, Context context) {
        this.groupId = groupId;
        this.database = LocalDatabase.getInstance(context);
        this.photoSyncDb = Room.inMemoryDatabaseBuilder(context, PhotoSyncDatabase.class).build();
        this.executor = ThreadTools.EXECUTOR;
        this.mainHandler = ThreadTools.MAIN_HANDLER;
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.photoEventListener = new PhotoEventListener();
        this.groupReference = this.firebaseDatabase.getReference("photos").child(groupId);
        this.context = context;
        this.notifier = Notifier.getInstance();
        this.timer = new Timer();
        //this.CURRENT_RESOLUTION_LEVEL;// = getCurrentResolution();

    }

    public interface Factory {
        PhotoSynchronizer create(String groupId);
    }

    public void listen() {
        groupReference.addChildEventListener(photoEventListener);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                downloadAllImprovablePhotos();
                uploadAllImprovablePhotos();
            }
        }, 0, SCHEDULE_PERIOD);
    }

    public void stop() {
        groupReference.removeEventListener(photoEventListener);

        timer.cancel();
    }

    public void uploadPhoto(final String photoId) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                new PhotoUpload(photoId).start();
            }
        });
    }

    private String getCurrentResolution() {
        Map<String, ?> settings = PreferenceManager.getDefaultSharedPreferences(context).getAll();
        int status = CheckNetworkConnection.getConnectivityStatusString(context);

        if(status == CheckNetworkConnection.NETWORK_STATUS_WIFI) {
            System.out.println("WIFI!!");
            System.out.println("SETTING: " + settings.get("pref_imgQuality_wifi"));
            //this.CURRENT_RESOLUTION_LEVEL = changeResolutionFormat(settings.get("pref_imgQuality_wifi").toString());
            return changeResolutionFormat(settings.get("pref_imgQuality_wifi").toString());

        } else if(status == CheckNetworkConnection.NETWORK_STATUS_MOBILE) {
            System.out.println("MOBILE!!");
            System.out.println("SETTING: " + settings.get("pref_imgQuality_mobile"));

            return changeResolutionFormat(settings.get("pref_imgQuality_mobile").toString());
        } else {
            // use wifi setting if no connection, it doesn't matter
            return changeResolutionFormat(settings.get("pref_imgQuality_wifi").toString());
        }


    }
    private String changeResolutionFormat(String format) {
        if(format.equals("640x480")) {
            System.out.println("640x480");
            return ResolutionTools.LEVEL_LOW;
        } else if(format.equals("1280x960")) {
            System.out.println("1280x960");
            return ResolutionTools.LEVEL_HIGH;
        }
        System.out.println("FULL");
        return ResolutionTools.LEVEL_FULL;
    }

    // This should be called when network/settings change.
    @WorkerThread
    public void downloadAllImprovablePhotos() {
        int maxResolution = ResolutionTools.getResolution(getCurrentResolution());

        final String[] photoIds = database.galleryDao().loadPhotosWithHigherOnlineResolution(
                groupId, maxResolution);

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

    // This should be called when network/settings change.
    @WorkerThread
    public void uploadAllImprovablePhotos() {
        int maxResolution = ResolutionTools.getResolution(getCurrentResolution());

        final String[] photoIds = database.galleryDao().loadPhotosWithHigherLocalResolution(
                groupId, maxResolution);

        for (final String photoId : photoIds) {
            if (shouldUpload(photoId)) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        new PhotoUpload(photoId).start();
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

        int sourceResolution = ResolutionTools.getResolution(getCurrentResolution(),
                resolution.online);

        return sourceResolution > resolution.local;
    }

    @WorkerThread
    private boolean shouldUpload(String photoId) {
        Photo.ResolutionInfo resolution = database.galleryDao().loadPhotoResolution(photoId);

        if (resolution == null)
            return false;

        int sourceResolution = ResolutionTools.getResolution(getCurrentResolution(),
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
                notifier.notifyAddPhoto(groupId, context);
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
            if (dataSnapshot.hasChild("files/" + getCurrentResolution())
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
                final boolean people = dataSnapshot.child("people").getValue(boolean.class);
                database.galleryDao().updatePhotoPeople(photoId, people ? 1 : 0);
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
            final LiveData<PhotoSyncLock> observable = photoSyncDb.dao().get(photoId);

            observable.observeForever(new AsyncObserver<PhotoSyncLock>(executor, mainHandler) {
                @Override
                @WorkerThread
                protected void onChangedAsync(@Nullable PhotoSyncLock photoSyncLock) {
                    if (photoSyncLock != null) return;

                    if (photoSyncDb.dao().tryStart(photoId)) {
                        removeFrom(observable);
                        run();
                    }
                }
            });
        }

        @WorkerThread
        protected void release() {
            photoSyncDb.dao().release(new PhotoSyncLock(photoId));
        }

        @WorkerThread
        protected abstract void run();
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

            String current_resolution_level = getCurrentResolution();

            Photo.FileInfo onlineFileInfo = new Photo.FileInfo();
            onlineFileInfo.file = UUID.randomUUID().toString() + ".jpg";
            onlineFileInfo.resolution = ResolutionTools.getResolution(current_resolution_level, localFileInfo.resolution);

            volatileFileInfo = onlineFileInfo;

            File localFile = FileTools.get(localFileInfo.file);

            StorageReference fileRef = firebaseStorage.getReference("images")
                    .child(groupId).child(onlineFileInfo.file);

            FileUploadListener listener = new FileUploadListener();

            // Resolution needs to be downgraded
            if(current_resolution_level != ResolutionTools.LEVEL_FULL) {
                byte[] decodedfile = decodeFile(localFile);
                if(decodedfile != null) {
                    System.out.println("Uploading resolution " + ResolutionTools.getResolution(current_resolution_level) + " to server");
                    fileRef.putBytes(decodedfile)
                            .addOnSuccessListener(executor, listener)
                            .addOnFailureListener(executor, listener);

                } else {
                    System.out.println("Failed to change the resolution of the image");
                }
            } else {
                System.out.println("Uploading full resolution of the image");
                fileRef.putFile(Uri.fromFile(localFile))
                        .addOnSuccessListener(executor, listener)
                        .addOnFailureListener(executor, listener);
            }

            release();
        }

        private byte[] decodeFile(File f) {
            try {
                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                // The new size we want to scale to
                final int REQUIRED_SIZE = ResolutionTools.getResolution(getCurrentResolution());

                int scale = 1;
                if (o.outHeight > REQUIRED_SIZE ) {
                    scale = (int)Math.pow(2, (int) Math.ceil(Math.log(REQUIRED_SIZE /
                            (double) o.outHeight) / Math.log(0.5)));
                }
                // Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                Bitmap bitmap =  BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

                // Change Bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                return stream.toByteArray();

            } catch (FileNotFoundException e) {}
            return null;
        }

        private class FileUploadListener implements OnFailureListener,
                OnSuccessListener<UploadTask.TaskSnapshot> {

            @Override
            public void onFailure(@NonNull Exception e) {
                release();
            }

            @Override
            public void onSuccess(UploadTask.TaskSnapshot result) {
                DatabaseReference filenameRef = firebaseDatabase.
                        getReference("photos").child(groupId).child(photoId).child("files/full");

                FilenamePushListener listener = new FilenamePushListener();
                filenameRef.setValue(volatileFileInfo.file)
                        .addOnSuccessListener(executor, listener)
                        .addOnFailureListener(executor, listener);
            }
        }

        private class FilenamePushListener implements OnFailureListener,
                OnSuccessListener<Void> {

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
            photosRef.child(groupId).child(photoId).child("files").child(getCurrentResolution())
                    .addListenerForSingleValueEvent(new FilenamePullListener());
        }

        private class FilenamePullListener extends AsyncValueEventListener {

            public FilenamePullListener() {
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

                StorageReference fileRef = firebaseStorage.getReference("images")
                        .child(groupId).child(filename);

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

                database.galleryDao().improvePhotoOnlineResolution(photoId, resolution);

                release();
            }

            @Override
            @WorkerThread
            public void onFailure(@NonNull Exception e) {
                release();
            }
        }
    }

    @Database(entities = PhotoSyncLock.class, version = 1, exportSchema = false)
    static abstract class PhotoSyncDatabase extends RoomDatabase {
        public abstract PhotoSyncDao dao();
    }

    @Dao
    static abstract class PhotoSyncDao {

        @Query("SELECT EXISTS(SELECT 1 FROM PhotoSyncLock WHERE photoId = :photoId LIMIT 1)")
        protected abstract boolean has(String photoId);

        @Query("SELECT * FROM PhotoSyncLock WHERE photoId = :photoId")
        public abstract LiveData<PhotoSyncLock> get(String photoId);

        @Insert
        protected abstract void start(PhotoSyncLock photoSyncLock);

        @Delete
        public abstract void release(PhotoSyncLock photoSyncLock);

        @Transaction
        public boolean tryStart(String photoId) {
            if (has(photoId))
                return false;
            start(new PhotoSyncLock(photoId));
            return true;
        }

        @Query("DELETE FROM PhotoSyncLock")
        public abstract void releaseAll();
    }

    @Entity
    static class PhotoSyncLock {
        @PrimaryKey
        @NonNull
        public final String photoId;

        public PhotoSyncLock(String photoId) {
            this.photoId = photoId;
        }
    }
}
