package com.hfad.guineapiglog

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.hfad.guineapiglog.entities.EventPet

@Dao
interface EventPetDao {
    @Insert
    suspend fun insert(eventPet : EventPet)

    @Delete
    suspend fun delete(eventPet : EventPet)

    @Update
    suspend fun update(eventPet : EventPet)
}