package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;


@Entity(indices = @Index("albumKey"))
public class Photo {

    public static final int PEOPLE_NA = -1;
    public static final int PEOPLE_NO = 0;
    public static final int PEOPLE_YES = 1;

    @PrimaryKey
    @NonNull
    private String key = "";
    private String author;
    private String path;
    private int peopleAppearance = PEOPLE_NA;
    private String albumKey;

    public Photo() {

    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAlbumKey() {
        return albumKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPeopleAppearance(int peopleAppearance) {
        this.peopleAppearance = peopleAppearance;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public String getPath()
    {
        return this.path;
    }

    public int getPeopleAppearance()
    {
        return this.peopleAppearance;
    }

    public static boolean areContentsTheSame(@NonNull Photo photo1, @NonNull Photo photo2) {
        if (photo1.peopleAppearance != photo2.peopleAppearance) return false;
        if (!TextUtils.equals(photo1.path, photo2.path)) return false;
        if (!TextUtils.equals(photo1.author, photo2.author)) return false;

        return true;
    }
}
