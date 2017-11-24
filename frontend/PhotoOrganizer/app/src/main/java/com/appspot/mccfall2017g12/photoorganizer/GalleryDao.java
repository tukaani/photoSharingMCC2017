package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
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

    @Delete
    public void deletePhotos(Photo... photos);

    @Query("SELECT * FROM Album NATURAL LEFT OUTER JOIN "
            + "(SELECT albumId, COUNT(*) AS photoCount, MIN(path) AS path "
            + "FROM Photo GROUP BY albumId)")
    LiveData<Album.Extended[]> loadAllAlbums();

    @Query("SELECT photoId, author, people, path, albumId, "
            + Photo.Extended.TYPE_ITEM + " AS itemType "
            + "FROM Photo WHERE albumId = :albumId UNION ALL "
            + "SELECT DISTINCT people || '', '', people, '', '', "
            + Photo.Extended.TYPE_PEOPLE_HEADER + " "
            + "FROM Photo WHERE albumId = :albumId "
            + "ORDER BY people, itemType DESC")
    LiveData<Photo.Extended[]> loadAlbumsPhotosByPeople(String albumId);

    @Query("SELECT photoId, author, people, path, albumId, "
            + Photo.Extended.TYPE_ITEM + " AS itemType "
            + "FROM Photo WHERE albumId = :albumId UNION ALL "
            + "SELECT DISTINCT author, author, 0, '', '', "
            + Photo.Extended.TYPE_AUTHOR_HEADER + " "
            + "FROM Photo WHERE albumId = :albumId "
            + "ORDER BY author, itemType DESC")
    LiveData<Photo.Extended[]> loadAlbumsPhotosByAuthor(String albumId);
}
