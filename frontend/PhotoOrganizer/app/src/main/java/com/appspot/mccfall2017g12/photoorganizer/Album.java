package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;

@Entity
public class Album {

    public static final String PRIVATE_ALBUM_ID = "PRIVATE";

    @PrimaryKey
    @NonNull
    public String albumId = "";
    public String name;

    public static class Extended implements Diffable<Extended> {

        @Embedded
        public Album album;
        public String file;
        public int photoCount = 0;

        @Override
        public boolean isTheSameAs(Extended other) {
            return TextUtils.equals(this.album.albumId, other.album.albumId);
        }

        @Override
        public boolean hasTheSameContentAs(Extended other) {
            if (this.photoCount != other.photoCount) return false;
            if (!TextUtils.equals(this.album.name, other.album.name)) return false;
            if (!TextUtils.equals(this.file, other.file)) return false;

            return true;
        }
    }
}
