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

    // left join I think
    @Query("SELECT photo_table.photo_id AS photo_id, photo_date, photo_height, photo_width, photo_name, photo_uri, photo_filesize " +
            "FROM photo_table, photo_event_table " +
            "WHERE :eventId=photo_event_table.event_id AND photo_table.photo_id=photo_event_table.photo_id")
    suspend fun fetchPhotosOfEvent(eventId: Long): List<Photo>
}