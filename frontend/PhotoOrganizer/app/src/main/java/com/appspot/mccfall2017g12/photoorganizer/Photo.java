package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;


@Entity(indices = @Index("albumKey"))
public class Photo implements Diffable<Photo> {

    public static final int PEOPLE_NA = -1;
    public static final int PEOPLE_NO = 0;
    public static final int PEOPLE_YES = 1;

    @PrimaryKey
    @NonNull
    public String photoKey = "";
    public String author;
    public String path;
    public int peopleAppearance = PEOPLE_NA;
    public String albumKey;

    @Override
    public boolean isTheSameAs(Photo other) {
        return TextUtils.equals(this.photoKey, other.photoKey);
    }

    @Override
    public boolean hasTheSameContentAs(Photo other) {
        if (this.peopleAppearance != other.peopleAppearance) return false;
        if (!TextUtils.equals(this.path, other.path)) return false;
        if (!TextUtils.equals(this.author, other.author)) return false;

        return true;
    }
}
