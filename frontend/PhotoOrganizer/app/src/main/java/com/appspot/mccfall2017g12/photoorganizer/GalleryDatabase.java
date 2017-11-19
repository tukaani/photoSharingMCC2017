package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Album.class, Photo.class}, version = 1)
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

    public static class LoadPhotosTask
            extends RelayPostExecutionTask<String, Void, LiveData<Photo[]>> {

        @Override
        protected LiveData<Photo[]> doInBackground(String... params) {
            String albumKey = params[0];
            LiveData<Photo[]> photos = GalleryDatabase.getInstance().galleryDao()
                    .loadAlbumsPhotosByAuthor(albumKey);
            return photos;
        }
    }

    public static class LoadAlbumsTask extends RelayPostExecutionTask<Void, Void, Album[]> {

        @Override
        protected Album[] doInBackground(Void... voids) {
            Album[] albums = GalleryDatabase.getInstance().galleryDao().loadAllAlbums();
            return albums;
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
