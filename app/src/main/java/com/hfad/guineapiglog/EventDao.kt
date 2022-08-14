package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: Event): Long

    @Delete
    suspend fun delete(event: Event)

    @Update
    suspend fun update(event: Event)

    @Query("SELECT * FROM event_table WHERE event_id = :eventId")
    suspend fun get(eventId: Long): Event

    // TODO: "ORDER BY event_date DESCENDING"
    @Query("SELECT * FROM event_table ORDER BY event_date")
    suspend fun getAll(): MutableList<Event>

    @Query("SELECT pet_table.pet_id AS pet_id, pet_name, pet_species, pet_breed, pet_sex, pet_dob, has_dob FROM pet_table LEFT JOIN event_pet_table WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    suspend fun getPetsOfEvent(eventId: Long): MutableList<Pet>
}