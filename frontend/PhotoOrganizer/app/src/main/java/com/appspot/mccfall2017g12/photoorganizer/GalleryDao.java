package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.support.annotation.Nullable;
import android.text.TextUtils;

@Dao
public abstract class GalleryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertPhotos(Photo... photos);

    @Query("SELECT file, resolution_local as resolution FROM Photo WHERE photoId = :photoId")
    public abstract Photo.FileInfo loadPhotoFileInfo(String photoId);

    @Query("SELECT EXISTS(SELECT 1 FROM Photo WHERE photoId = :photoId LIMIT 1)")
    public abstract boolean photoExists(String photoId);

    @Query("UPDATE Photo SET people = :people WHERE photoId = :photoId AND people <> :people")
    public abstract void updatePhotoPeople(String photoId, int people);

    @Query("UPDATE Photo SET resolution_online = :onlineResolution WHERE photoId = :photoId "
            + "AND resolution_online < :onlineResolution")
    protected abstract void improvePhotoOnlineResolution(String photoId, int onlineResolution);

    @Query("SELECT resolution_local AS local, resolution_online AS online "
            + "FROM Photo WHERE photoId = :photoId")
    public abstract Photo.ResolutionInfo loadPhotoResolution(String photoId);

    @Query("UPDATE Photo SET file = :file, resolution_local = :resolution WHERE photoId = :photoId")
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
        Photo.FileInfo photoFileInfo = loadPhotoFileInfo(photoId);

        if (photoFileInfo == null)
            return file; // No file is used because the photo does not exist.

        String wasteFile;

        if (resolution > photoFileInfo.resolution) {
            updatePhotoFile(photoId, file, resolution);
            wasteFile = photoFileInfo.file; // File changed, the old file is no longer used.
        }
        else {
            wasteFile = file; // The new file is not used because it doesn't improve resolution.
        }

        if (TextUtils.equals(file, photoFileInfo.file))
            return null; // The new file is the same as the old one. No file should be deleted.

        return wasteFile;
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAlbums(Album... albums);

    @Delete
    public abstract void deletePhotos(Photo... photos);

    @Transaction
    @Query("SELECT photoId FROM Photo WHERE albumId = :albumId AND "
            + "resolution_online > resolution_local AND resolution_local < :maxResolution")
    public abstract String[] loadPhotosWithHigherOnlineResolution(String albumId,
                                                                  int maxResolution);

    @Transaction
    @Query("SELECT photoId FROM Photo WHERE albumId = :albumId AND "
            + "resolution_local > resolution_online AND resolution_online < :maxResolution")
    public abstract String[] loadPhotosWithHigherLocalResolution(String albumId, int maxResolution);

    @Transaction
    @Query("SELECT * FROM Album NATURAL LEFT OUTER JOIN "
            + "(SELECT albumId, COUNT(*) AS photoCount, MIN(file) AS file "
            + "FROM Photo GROUP BY albumId)")
    public abstract LiveData<Album.Extended[]> loadAllAlbums();

    @Transaction
    @Query("SELECT photoId, author, people, file, albumId, resolution_local, resolution_online, "
            + Photo.Extended.TYPE_ITEM + " AS itemType "
            + "FROM Photo WHERE albumId = :albumId UNION ALL "
            + "SELECT DISTINCT people || '', '', people, '', '', 0, 0, "
            + Photo.Extended.TYPE_PEOPLE_HEADER + " "
            + "FROM Photo WHERE albumId = :albumId "
            + "ORDER BY people, itemType DESC")
    public abstract LiveData<Photo.Extended[]> loadAlbumsPhotosByPeople(String albumId);

    @Transaction
    @Query("SELECT photoId, author, people, file, albumId, resolution_local, resolution_online, "
            + Photo.Extended.TYPE_ITEM + " AS itemType "
            + "FROM Photo WHERE albumId = :albumId UNION ALL "
            + "SELECT DISTINCT author, author, 0, '', '', 0, 0, "
            + Photo.Extended.TYPE_AUTHOR_HEADER + " "
            + "FROM Photo WHERE albumId = :albumId "
            + "ORDER BY author, itemType DESC")
    public abstract LiveData<Photo.Extended[]> loadAlbumsPhotosByAuthor(String albumId);
}
