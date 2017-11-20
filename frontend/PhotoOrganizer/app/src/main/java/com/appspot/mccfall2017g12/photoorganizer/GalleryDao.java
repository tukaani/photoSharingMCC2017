package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.arch.lifecycle.LiveData;

@Dao
public interface GalleryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPhotos(Photo... photos);

    @Update
    void updatePhotos(Photo... photos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAlbums(Album... albums);

    @Query("SELECT * FROM Album NATURAL LEFT OUTER JOIN "
            + "(SELECT albumKey, COUNT(*) AS photoCount, MIN(path) AS path "
            + "FROM Photo GROUP BY albumKey)")
    LiveData<Album.Extended[]> loadAllAlbums();

    @Query("SELECT * FROM Photo WHERE albumKey = :albumKey ORDER BY peopleAppearance")
    LiveData<Photo[]> loadAlbumsPhotosByPeopleAppearance(String albumKey);

    @Query("SELECT * FROM Photo WHERE albumKey = :albumKey ORDER BY author")
    LiveData<Photo[]> loadAlbumsPhotosByAuthor(String albumKey);
}
