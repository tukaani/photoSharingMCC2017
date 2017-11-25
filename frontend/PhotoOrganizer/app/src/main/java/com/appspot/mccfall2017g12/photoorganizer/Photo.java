package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;


@Entity(indices = @Index("albumId"))
public class Photo {

    public static final int PEOPLE_NA = -1;
    public static final int PEOPLE_NO = 0;
    public static final int PEOPLE_YES = 1;

    @PrimaryKey
    @NonNull
    public String photoId = "";
    public String author;
    public String file;
    public int people = PEOPLE_NA;
    public String albumId;
    public int resolution;
    public int onlineResolution;

    public static class Extended implements Diffable<Extended>, CategoryItem {

        protected static final int TYPE_ITEM = 0;
        protected static final int TYPE_PEOPLE_HEADER = 1;
        protected static final int TYPE_AUTHOR_HEADER = 2;

        @Embedded
        public Photo photo;
        public int itemType;

        @Override
        public boolean isTheSameAs(Extended other) {
            return this.itemType == other.itemType
                    && TextUtils.equals(this.photo.photoId, other.photo.photoId);
        }

        @Override
        public boolean hasTheSameContentAs(Extended other) {
            if (this.itemType != other.itemType) return false;

            if (this.itemType != TYPE_ITEM) return true;

            if (this.photo.people != other.photo.people) return false;
            if (!TextUtils.equals(this.photo.file, other.photo.file)) return false;
            if (!TextUtils.equals(this.photo.author, other.photo.author)) return false;

            return true;
        }

        @Override
        public boolean isHeader() {
            return this.itemType > TYPE_ITEM;
        }
    }
}
