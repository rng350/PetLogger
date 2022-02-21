package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {
    @Insert
    fun insert(event: Event)

    @Delete
    fun delete(event: Event)

    @Update
    fun update(event: Event)

    @Query("SELECT * FROM event_table WHERE event_id = :eventId")
    fun get(eventId: Long): LiveData<Event>

    // TODO: "ORDER BY event_date DESCENDING"
    @Query("SELECT * FROM event_table ORDER BY event_date")
    fun getAll(): LiveData<List<Event>>

    @Query("SELECT pet_table.pet_id AS pet_id, pet_name, pet_species, pet_breed, pet_sex, pet_dob, has_dob FROM pet_table LEFT JOIN event_pet_table WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    fun getPetsOfEvent(eventId: Long): LiveData<List<Pet>>

    @Query("SELECT event_table.event_id AS event_id, event_title, event_details, event_date FROM event_table, event_pet_table WHERE event_pet_table.pet_id=:petId AND event_table.event_id=event_pet_table.event_id ORDER BY event_date")
    fun getEventsOfPet(petId: Long): LiveData<List<Event>>
}