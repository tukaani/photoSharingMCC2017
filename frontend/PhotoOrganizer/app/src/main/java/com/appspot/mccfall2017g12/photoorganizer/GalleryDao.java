package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.lifecycle.LiveData;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

@Dao
public abstract class GalleryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertPhotos(Photo... photos);

    @Query("SELECT * FROM Photo WHERE photoId = :photoId")
    public abstract Photo loadPhoto(String photoId);

    @Query("UPDATE Photo SET people = :people WHERE photoId = :photoId")
    public abstract void updatePhotoPeople(String photoId, int people);

    @Query("UPDATE Photo SET onlineResolution = :onlineResolution WHERE photoId = :photoId")
    public abstract void updatePhotoOnlineResolution(String photoId, int onlineResolution);

    @Query("UPDATE Photo SET file = :file, resolution = :resolution WHERE photoId = :photoId")
    public abstract void updatePhotoFile(String photoId, String file, int resolution);

    @Query("UPDATE Photo SET author = :author WHERE photoId = :photoId")
    public abstract void updatePhotoAuthor(String photoId, String author);

    /**
     * Changes the image file of a photo if the new image file has a higher resolution than
     * the old one.
     *
     * @param photoId ID of the photo
     * @param file Name of the new file
     * @param resolution Resolution of the new file
     * @return The file that is NOT used anymore and thus, can be deleted. Can be null.
     */
    @Transaction
    @Nullable
    public String tryUpdatePhotoFile(String photoId, String file, int resolution) {
        Photo photo = loadPhoto(photoId);

        if (photo == null)
            return file; // No file is used because the photo does not exist.

        if (resolution > photo.resolution) {
            updatePhotoFile(photoId, file, resolution);
            return photo.file; // File changed, the old file is no longer used.
        }

        if (TextUtils.equals(file, photo.file))
            return null; // The file is the same as the old one. No file should be deleted.

        return file; // The new file is not used because it does not improve resolution.
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAlbums(Album... albums);

    @Delete
    public abstract void deletePhotos(Photo... photos);

    @Query("SELECT * FROM Album NATURAL LEFT OUTER JOIN "
            + "(SELECT albumId, COUNT(*) AS photoCount, MIN(file) AS file "
            + "FROM Photo GROUP BY albumId)")
    public abstract LiveData<Album.Extended[]> loadAllAlbums();

    @Query("SELECT photoId, author, people, file, albumId, resolution, onlineResolution, "
            + Photo.Extended.TYPE_ITEM + " AS itemType "
            + "FROM Photo WHERE albumId = :albumId UNION ALL "
            + "SELECT DISTINCT people || '', '', people, '', '', 0, 0, "
            + Photo.Extended.TYPE_PEOPLE_HEADER + " "
            + "FROM Photo WHERE albumId = :albumId "
            + "ORDER BY people, itemType DESC")
    public abstract LiveData<Photo.Extended[]> loadAlbumsPhotosByPeople(String albumId);

    @Query("SELECT photoId, author, people, file, albumId, resolution, onlineResolution, "
            + Photo.Extended.TYPE_ITEM + " AS itemType "
            + "FROM Photo WHERE albumId = :albumId UNION ALL "
            + "SELECT DISTINCT author, author, 0, '', '', 0, 0, "
            + Photo.Extended.TYPE_AUTHOR_HEADER + " "
            + "FROM Photo WHERE albumId = :albumId "
            + "ORDER BY author, itemType DESC")
    public abstract LiveData<Photo.Extended[]> loadAlbumsPhotosByAuthor(String albumId);
}
