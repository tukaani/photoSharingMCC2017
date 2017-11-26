package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Album.class, Photo.class, PhotoSyncLock.class},
        version = 1, exportSchema = false)
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

    public abstract static class Task<Params, Result>
            extends RelayPostExecutionTask<Params, Void, Result> {

    }

    public static class InsertPhotoTask extends Task<Photo, Void> {

        @Override
        protected Void doInBackground(Photo... photos) {
            GalleryDatabase.getInstance().galleryDao().insertPhotos(photos);
            return null;
        }
    }

    public static class InsertAlbumTask extends Task<Album, Void> {

        @Override
        protected Void doInBackground(Album... albums) {
            GalleryDatabase.getInstance().galleryDao().insertAlbums(albums);
            return null;
        }
    }

    public static class DeletePhotoTask extends Task<Photo, Void> {

        @Override
        protected Void doInBackground(Photo... photos) {
            GalleryDatabase.getInstance().galleryDao().deletePhotos(photos);
            return null;
        }
    }
}
