package com.hfad.guineapiglog.dao

import androidx.room.*
import com.hfad.guineapiglog.entities.Event
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.PetWithProfilePic
import com.hfad.guineapiglog.entities.Photo

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

    @Query("SELECT pet_table.pet_id AS pet_id, pet_name, pet_species, pet_breed, pet_sex, pet_dob, has_dob " +
            "FROM pet_table LEFT JOIN event_pet_table " +
            "WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    suspend fun getPetsOfEvent(eventId: Long): MutableList<Pet>

    @Query("SELECT photo_table.photo_id AS photo_id, photo_name, photo_uri, photo_width, photo_height, photo_filesize, photo_date " +
            "FROM photo_table LEFT JOIN photo_event_table " +
            "WHERE photo_event_table.event_id=:eventId AND photo_table.photo_id=photo_event_table.photo_id")
    suspend fun fetchPhotosOfEvent(eventId: Long): List<Photo>

    @Query("SELECT pet_table.pet_id AS pet_id, pet_name, pet_species, pet_breed, pet_sex, pet_dob, has_dob " +
            "FROM pet_table LEFT JOIN event_pet_table " +
            "WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    suspend fun getPetsOfEventWithProfilePhotos(eventId: Long): List<PetWithProfilePic>
}