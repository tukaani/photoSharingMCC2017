package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class PhotoEventListener implements ChildEventListener {

    private final String groupId;
    private final GalleryDatabase database;

    //TODO hack, must be changed!
    public volatile static boolean isListening = false;

    //TODO Should be calculated based on network state & settings
    private final static int CURRENT_RESOLUTION_LEVEL = ResolutionTools.LEVEL_HIGH;

    public PhotoEventListener(String groupId, Context context) {
        this.groupId = groupId;

        GalleryDatabase.initialize(context);
        this.database = GalleryDatabase.getInstance();
    }

    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String previousChildName) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String photoId = dataSnapshot.getKey();

                Photo photo = database.galleryDao().loadPhoto(photoId);

                if (photo == null) {
                    photo = new Photo();
                    photo.photoId = photoId;
                    photo.albumId = groupId;
                    database.galleryDao().insertPhotos(photo);
                    fetchAndSetAuthorNameForPhotoAsync(dataSnapshot);
                }

                updatePhotoPeopleAndOnlineResolution(dataSnapshot);
                updateImageIfNecessary(photoId);
            }
        }).start();
    }

    @Override
    public void onChildChanged(final DataSnapshot dataSnapshot, String previousChildName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updatePhotoPeopleAndOnlineResolution(dataSnapshot);
                updateImageIfNecessary(dataSnapshot.getKey());
            }
        }).start();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

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
    private void updatePhotoPeopleAndOnlineResolution(final DataSnapshot dataSnapshot) {
        String photoId = dataSnapshot.getKey();
        if (dataSnapshot.hasChild("people")) {
            final int people = dataSnapshot.child("people").getValue(int.class);
            database.galleryDao().updatePhotoPeople(photoId, people);
        }
        if (dataSnapshot.hasChild("resolution")) {
            final int onlineResolution = dataSnapshot.child("resolution").getValue(int.class);
            database.galleryDao().updatePhotoOnlineResolution(photoId, onlineResolution);
        }
    }

    // Don't call from UI thread
    private void updateImageIfNecessary(String photoId) {
        Photo photo = database.galleryDao().loadPhoto(photoId);

        int targetResolution = ResolutionTools.getResolution(CURRENT_RESOLUTION_LEVEL,
                photo.onlineResolution);

        if (targetResolution > photo.resolution) {
            DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference("photos");
            photosRef.child(groupId).child(photoId).child("files")
                    .child(Integer.toString(CURRENT_RESOLUTION_LEVEL))
                    .addListenerForSingleValueEvent(
                            new PhotoFileListener(photoId, targetResolution));
        }
    }

    private class UserNameEventListener implements ValueEventListener {

        private final String photoId;

        public UserNameEventListener(String photoId) {

            this.photoId = photoId;
        }

        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String userName = dataSnapshot.getValue(String.class);
                    database.galleryDao().updatePhotoAuthor(photoId, userName);
                }
            }).start();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class PhotoFileListener implements ValueEventListener,
            OnSuccessListener<FileDownloadTask.TaskSnapshot> {

        private final String photoId;
        private final int resolution;
        private String filename;

        public PhotoFileListener(String photoId, int resolution) {

            this.photoId = photoId;
            this.resolution = resolution;
        }

        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {
            this.filename = dataSnapshot.getValue(String.class);

            StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images");
            StorageReference fileRef = imagesRef.child(groupId).child(filename);

            File localFile = FileTools.get(filename);
            fileRef.getFile(localFile).addOnSuccessListener(PhotoFileListener.this);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            final String filename = this.filename;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!database.galleryDao().tryUpdatePhotoFile(photoId, filename, resolution))
                        FileTools.get(filename).delete();
                }
            }).start();
        }
    }
}
