package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PetDao {
    @Insert
    suspend fun insert(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Update
    suspend fun update(pet: Pet)

    @Query("SELECT * FROM pet_table WHERE pet_id=:petId")
    fun get(petId: Long): LiveData<Pet>

    @Query("SELECT * FROM pet_table")
    fun getAll(): LiveData<MutableList<Pet>>

    @Query("SELECT event_table.event_id AS event_id, event_title, event_details, event_date FROM event_table, event_pet_table WHERE event_pet_table.pet_id=:petId AND event_table.event_id=event_pet_table.event_id ORDER BY event_date")
    fun getEventsOfPet(petId: Long): LiveData<MutableList<Event>>
}