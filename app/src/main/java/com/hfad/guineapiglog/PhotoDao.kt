package com.hfad.guineapiglog

import androidx.room.*

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: Photo)

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photo_table")
    suspend fun getAllPhotos()

    @Insert
    suspend fun insert(photoEvent: PhotoEvent)

    @Update
    suspend fun update(photoEvent: PhotoEvent)

    @Delete
    suspend fun delete(photoEvent: PhotoEvent)
}