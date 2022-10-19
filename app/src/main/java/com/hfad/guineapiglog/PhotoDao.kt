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
}