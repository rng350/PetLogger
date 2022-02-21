package com.hfad.guineapiglog

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert

@Dao
interface EventPetDao {
    @Insert
    fun insert(eventPet : EventPet)

    @Delete
    fun delete(eventPet : EventPet)
}