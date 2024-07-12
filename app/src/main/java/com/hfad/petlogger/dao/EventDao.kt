package com.hfad.petlogger.dao

import androidx.room.*
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT * FROM event_table ORDER BY event_date DESC")
    suspend fun getAll(): MutableList<Event>

    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN event_pet_table " +
            "WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    suspend fun getPetsOfEvent(eventId: Long): List<Pet>

    @Query("SELECT photo_table.photo_id AS photo_id, photo_title, photo_filename, photo_uri, photo_width, photo_height, photo_filesize, photo_date " +
            "FROM photo_table LEFT JOIN photo_event_table " +
            "WHERE photo_event_table.event_id=:eventId AND photo_table.photo_id=photo_event_table.photo_id")
    suspend fun fetchPhotosOfEvent(eventId: Long): List<Photo>

    @Transaction
    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN event_pet_table " +
            "WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    suspend fun getPetsOfEventWithProfilePhotos(eventId: Long): List<PetWithProfilePic>

    @Query("SELECT photo_table.photo_id AS photo_id, photo_title, photo_filename, photo_uri, photo_width, photo_height, photo_filesize, photo_date " +
            "FROM photo_table LEFT JOIN photo_event_table " +
            "WHERE photo_event_table.event_id=:eventId AND photo_table.photo_id=photo_event_table.photo_id")
    fun getPhotosOfEventAsFlow(eventId: Long): Flow<List<Photo>>

    @Transaction
    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN event_pet_table " +
            "WHERE event_id = :eventId AND pet_table.pet_id = event_pet_table.pet_id")
    fun getPetsOfEventWithProfilePhotosAsFlow(eventId: Long): Flow<List<PetWithProfilePic>>

    @Query("SELECT * FROM event_table")
    fun getAllEventsAsFlow(): Flow<List<Event>>

    @Query("SELECT note_table.* " +
            "FROM note_table LEFT JOIN event_note_table " +
            "ON note_table.note_id=event_note_Table.note_id " +
            "WHERE event_note_table.event_id=:eventId")
    suspend fun getNotesOfEvent(eventId: Long): List<Note>

    @Insert
    suspend fun attachNotes(notes: List<EventNote>)

    @Query("SELECT note_table.* " +
            "FROM note_table LEFT JOIN event_note_table " +
            "ON note_table.note_id=event_note_Table.note_id " +
            "WHERE event_note_table.event_id=:eventId")
    fun getNotesOfEventAsFlow(eventId: Long): Flow<List<Note>>
}