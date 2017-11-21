package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Album.class, Photo.class}, version = 1, exportSchema = false)
public abstract class GalleryDatabase extends RoomDatabase {

    private static GalleryDatabase instance;

    public static GalleryDatabase getInstance() {
        return instance;
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                    GalleryDatabase.class, "database-name").build();
        }
    }

    public abstract GalleryDao galleryDao();

    public static class LoadPhotosByAuthorTask
            extends RelayPostExecutionTask<String, Void, LiveData<Photo.Extended[]>> {

        @Override
        protected LiveData<Photo.Extended[]> doInBackground(String... params) {
            String albumKey = params[0];
            return GalleryDatabase.getInstance().galleryDao()
                    .loadAlbumsPhotosByAuthor(albumKey);
        }
    }

    public static class LoadPhotosByPeopleTask
            extends RelayPostExecutionTask<String, Void, LiveData<Photo.Extended[]>> {

        @Override
        protected LiveData<Photo.Extended[]> doInBackground(String... params) {
            String albumKey = params[0];
            return GalleryDatabase.getInstance().galleryDao()
                    .loadAlbumsPhotosByPeopleAppearance(albumKey);
        }
    }

    public static class LoadAlbumsTask
            extends RelayPostExecutionTask<Void, Void, LiveData<Album.Extended[]>> {

        @Override
        protected LiveData<Album.Extended[]> doInBackground(Void... voids) {
            return GalleryDatabase.getInstance().galleryDao().loadAllAlbums();
        }
    }

    public static class InsertPhotoTask extends RelayPostExecutionTask<Photo, Void, Void> {

        @Override
        protected Void doInBackground(Photo... photos) {
            GalleryDatabase.getInstance().galleryDao().insertPhotos(photos);
            return null;
        }
    }

    public static class InsertAlbumTask extends RelayPostExecutionTask<Album, Void, Void> {

        @Override
        protected Void doInBackground(Album... albums) {
            GalleryDatabase.getInstance().galleryDao().insertAlbums(albums);
            return null;
        }
    }
}
