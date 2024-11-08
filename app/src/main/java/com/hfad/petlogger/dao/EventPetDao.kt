package com.hfad.petlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.hfad.petlogger.common.associationentities.EventPet

@Dao
interface EventPetDao {
    @Insert
    suspend fun insert(eventPet : EventPet)

    @Delete
    suspend fun delete(eventPet : EventPet)

    @Update
    suspend fun update(eventPet : EventPet)
}