package com.hfad.guineapiglog.dao

import androidx.room.*
import com.hfad.guineapiglog.entities.PetProfilePhoto
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.entities.PhotoEvent

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo)

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

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
}