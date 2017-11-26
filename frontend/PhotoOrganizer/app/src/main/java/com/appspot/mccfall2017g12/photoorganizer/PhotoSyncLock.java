package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class PhotoSyncLock {
    @PrimaryKey
    @NonNull
    public String photoId;

    public PhotoSyncLock(String photoId) {
        this.photoId = photoId;
    }
}
