package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.concurrent.Executors;

public class PhotoEventListener extends AsyncChildEventListener {

    private final String groupId;
    private final GalleryDatabase database;

    //TODO hack, must be changed!
    public volatile static boolean isListening = false;

    //TODO Should be calculated based on network state & settings
    private final static String CURRENT_RESOLUTION_LEVEL = ResolutionTools.LEVEL_HIGH;

    public PhotoEventListener(String groupId, Context context) {
        super(Executors.newCachedThreadPool());
        this.groupId = groupId;

        GalleryDatabase.initialize(context);
        this.database = GalleryDatabase.getInstance();
    }

    @Override
    public void onChildAddedAsync(final DataSnapshot dataSnapshot, String previousChildName) {
        String photoId = dataSnapshot.getKey();

        Photo photo = database.galleryDao().loadPhoto(photoId);

        if (photo == null) {
            photo = new Photo();
            photo.photoId = photoId;
            photo.albumId = groupId;
            database.galleryDao().insertPhotos(photo);
            fetchAndSetAuthorNameForPhotoAsync(dataSnapshot);
        }

        updatePhoto(dataSnapshot);
    }

    @Override
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
    public void onCancelled(DatabaseError databaseError) {

    }

    private void fetchAndSetAuthorNameForPhotoAsync(final DataSnapshot dataSnapshot) {
        String photoId = dataSnapshot.getKey();
        String userId = dataSnapshot.child("author").getValue(String.class);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(userId).child("name").addListenerForSingleValueEvent(
                new UserNameEventListener(photoId));
    }

    // Don't call from UI thread
    private void updatePhoto(final DataSnapshot dataSnapshot) {
        String photoId = dataSnapshot.getKey();
        if (dataSnapshot.hasChild("people")) {
            final int people = dataSnapshot.child("people").getValue(int.class);
            database.galleryDao().updatePhotoPeople(photoId, people);
        }
        if (dataSnapshot.hasChild("resolution")) {
            final int onlineResolution = dataSnapshot.child("resolution").getValue(int.class);
            database.galleryDao().updatePhotoOnlineResolution(photoId, onlineResolution);
        }
        if (dataSnapshot.hasChild("files/" + CURRENT_RESOLUTION_LEVEL)) {
            updatePhotoFileIfNecessary(photoId);
        }
    }

    // Don't call from UI thread
    //
    // This method will be moved and made public because it should be called for each photo
    // (in the syncing album) whose online resolution is higher than its local resolution
    // whenever settings or network changes.
    private void updatePhotoFileIfNecessary(String photoId) {
        Photo photo = database.galleryDao().loadPhoto(photoId);

        if (photo == null)
            return;

        int targetResolution = ResolutionTools.getResolution(CURRENT_RESOLUTION_LEVEL,
                photo.onlineResolution);

        if (targetResolution > photo.resolution) {
            DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference("photos");
            photosRef.child(groupId).child(photoId).child("files").child(CURRENT_RESOLUTION_LEVEL)
                    .addListenerForSingleValueEvent(new PhotoFileListener(photoId));
        }
    }

    private class UserNameEventListener extends AsyncValueEventListener {

        private final String photoId;

        public UserNameEventListener(String photoId) {
            super(executor);

            this.photoId = photoId;
        }

        @Override
        public void onDataChangeAsync(final DataSnapshot dataSnapshot) {
            String userName = dataSnapshot.getValue(String.class);
            database.galleryDao().updatePhotoAuthor(photoId, userName);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class PhotoFileListener implements ValueEventListener,
            OnSuccessListener<FileDownloadTask.TaskSnapshot> {

        private final String photoId;
        private String filename;

        public PhotoFileListener(String photoId) {
            this.photoId = photoId;
        }

        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {
            if (filename != null)
                throw new IllegalStateException("PhotoFileListener can be used as a listener "
                        + "only for a single event and it cannot be reused.");

            if (!dataSnapshot.exists())
                return;

            filename = dataSnapshot.getValue(String.class);

            StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images");
            StorageReference fileRef = imagesRef.child(groupId).child(filename);

            File localFile = FileTools.get(filename);
            fileRef.getFile(localFile).addOnSuccessListener(this);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            final String filename = this.filename;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    File localFile = FileTools.get(filename);
                    int resolution = ResolutionTools.calculateResolution(
                            localFile.getAbsolutePath());

                    String fileToBeRemoved = database.galleryDao()
                            .tryUpdatePhotoFile(photoId, filename, resolution);

                    if (fileToBeRemoved != null)
                        FileTools.get(fileToBeRemoved).delete();
                }
            });
        }
    }
}
