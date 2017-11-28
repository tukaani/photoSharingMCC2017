package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Album.class, Photo.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {

    private static LocalDatabase instance;

    public static synchronized LocalDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context, LocalDatabase.class, "photoorganizer-db").build();
        }
        return instance;
    }

    public abstract GalleryDao galleryDao();
}
