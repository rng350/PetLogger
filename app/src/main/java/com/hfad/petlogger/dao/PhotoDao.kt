package com.hfad.petlogger.dao

import androidx.room.*
import com.hfad.petlogger.entities.PetProfilePhoto
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoEvent
import com.hfad.petlogger.entities.PhotoNote

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo)

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)


    @Query("SELECT * FROM photo_table WHERE photo_id=:photoId LIMIT 1")
    suspend fun getPhoto(photoId: Long): Photo

    @Query("SELECT * FROM photo_table")
    suspend fun getAllPhotos(): List<Photo>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photoEvent: PhotoEvent)

    @Update
    suspend fun update(photoEvent: PhotoEvent)

    @Delete
    suspend fun delete(photoEvent: PhotoEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(petProfilePhoto: PetProfilePhoto): Long

    @Update
    suspend fun update(petProfilePhoto: PetProfilePhoto)

    @Delete
    suspend fun delete(petProfilePhoto: PetProfilePhoto)

    @Insert
    suspend fun insert(photoNote: PhotoNote)

    @Update
    suspend fun update(photoNote: PhotoNote)

    @Delete
    suspend fun delete(photoNote: PhotoNote)
}