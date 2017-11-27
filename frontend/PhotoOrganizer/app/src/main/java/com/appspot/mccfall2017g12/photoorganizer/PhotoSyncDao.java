package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

@Dao
public abstract class PhotoSyncDao {

    @Query("SELECT EXISTS(SELECT 1 FROM PhotoSyncLock WHERE photoId = :photoId LIMIT 1)")
    protected abstract boolean has(String photoId);

    @Query("SELECT * FROM PhotoSyncLock WHERE photoId = :photoId")
    public abstract LiveData<PhotoSyncLock> get(String photoId);

    @Insert
    protected abstract void start(PhotoSyncLock photoSyncLock);

    @Delete
    public abstract void release(PhotoSyncLock photoSyncLock);

    @Transaction
    public boolean tryStart(String photoId) {
        if (has(photoId))
            return false;
        start(new PhotoSyncLock(photoId));
        return true;
    }

    @Query("DELETE FROM PhotoSyncLock")
    public abstract void releaseAll();
}
