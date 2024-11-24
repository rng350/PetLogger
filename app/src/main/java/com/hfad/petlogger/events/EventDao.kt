package com.hfad.petlogger.events

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.hfad.petlogger.common.associationentities.EventNote
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

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

    @Query("""
        SELECT * FROM event_table 
        WHERE (datetime(event_date), event_id) < (datetime(:lastEventDate), :lastEventId) 
        ORDER BY datetime(event_date) DESC, event_id DESC LIMIT :amtLimit
    """)
    suspend fun getAllEventsPaginated(lastEventDate: OffsetDateTime, lastEventId: Long, amtLimit: Int): List<Event>

    @Query("SELECT pet_table.pet_name AS petName, pet_table.pet_id AS petId, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN event_pet_table ON pet_table.pet_id=event_pet_table.pet_id " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON photo_table.photo_id=pet_profile_photo_table.photo_id " +
            "WHERE event_pet_table.event_id = :eventId " +
            "AND pet_table.pet_id > :lastPetId " +
            "ORDER BY pet_table.pet_id ASC LIMIT :amtLimit")
    suspend fun getPetsOfEventPaginated(eventId: Long, lastPetId: Long, amtLimit: Int): List<PetWithProfilePic>

    @Query("SELECT photo_table.photo_id AS photo_id, photo_title, photo_filename, photo_uri, photo_width, photo_height, photo_filesize, photo_date " +
            "FROM photo_table LEFT JOIN photo_event_table " +
            "WHERE photo_event_table.event_id=:eventId AND photo_table.photo_id=photo_event_table.photo_id")
    suspend fun fetchPhotosOfEvent(eventId: Long): List<Photo>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON photo_table.photo_id=pet_profile_photo_table.photo_id " +
            "LEFT JOIN event_pet_table ON pet_table.pet_id=event_pet_table.pet_id " +
            "LEFT JOIN event_table ON event_table.event_id=event_pet_table.event_id " +
            "WHERE event_table.event_id = :eventId " +
            "ORDER BY pet_table.pet_id ASC")
    suspend fun getPetsOfEventWithProfilePhotos(eventId: Long): List<PetWithProfilePic>

    @Query("SELECT photo_table.photo_id AS photo_id, photo_title, photo_filename, photo_uri, photo_width, photo_height, photo_filesize, photo_date " +
            "FROM photo_table LEFT JOIN photo_event_table " +
            "WHERE photo_event_table.event_id=:eventId AND photo_table.photo_id=photo_event_table.photo_id")
    fun getPhotosOfEventAsFlow(eventId: Long): Flow<List<Photo>>

    @Query("SELECT photo_table.photo_id AS photo_id, photo_title, photo_filename, photo_uri, photo_width, photo_height, photo_filesize, photo_date " +
            "FROM photo_table INNER JOIN photo_event_table " +
            "ON photo_table.photo_id=photo_event_table.photo_id " +
            "WHERE photo_event_table.event_id=:eventId " +
            "AND (datetime(photo_date), photo_table.photo_id) < (datetime(:lastPhotoDate), :lastPhotoId) " +
            "ORDER BY datetime(photo_date) DESC, photo_table.photo_id DESC LIMIT :amtLimit ")
    suspend fun getPhotosOfEventPaginated(eventId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table " +
            "ON pet_table.pet_id = pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table " +
            "ON photo_table.photo_id = pet_profile_photo_table.photo_id " +
            "LEFT JOIN event_pet_table " +
            "ON pet_table.pet_id = event_pet_table.pet_id " +
            "LEFT JOIN event_table " +
            "ON event_table.event_id = event_pet_table.event_id " +
            "WHERE event_table.event_id = :eventId")
    fun getPetsOfEventWithProfilePhotosAsFlow(eventId: Long): Flow<List<PetWithProfilePic>>

    @Query("SELECT * FROM event_table ORDER BY datetime(event_date) DESC")
    fun getAllEventsAsFlow(): Flow<List<Event>>

    @Query("SELECT note_table.* " +
            "FROM note_table LEFT JOIN event_note_table " +
            "ON note_table.note_id=event_note_Table.note_id " +
            "WHERE event_note_table.event_id=:eventId")
    suspend fun getNotesOfEvent(eventId: Long): List<Note>

    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN event_note_table " +
            "ON note_table.note_id=event_note_Table.note_id " +
            "WHERE event_note_table.event_id=:eventId " +
            "AND (datetime(note_last_updated), note_table.note_id) < (datetime(:lastNoteEditedDate), :lastNoteId) " +
            "ORDER BY datetime(note_last_updated) DESC, note_table.note_id DESC LIMIT :amtLimit")
    suspend fun getNotesOfEventPaginated(eventId: Long, lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, amtLimit: Int): List<Note>

    @Insert
    suspend fun attachNotes(notes: List<EventNote>)

    @Delete
    suspend fun detachNotes(notes: List<EventNote>)

    @Query("SELECT note_table.* " +
            "FROM note_table LEFT JOIN event_note_table " +
            "ON note_table.note_id=event_note_Table.note_id " +
            "WHERE event_note_table.event_id=:eventId")
    fun getNotesOfEventAsFlow(eventId: Long): Flow<List<Note>>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN event_tag_table 
        ON tag_table.tag_id=event_tag_table.tag_id 
        WHERE event_tag_table.event_id=:eventId 
        ORDER BY tag_table.tag_name ASC
    """)
    suspend fun getAllTagsOfEventAlphabeticalOrder(eventId: Long): List<Tag>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN event_tag_table 
        ON tag_table.tag_id=event_tag_table.tag_id 
        WHERE event_tag_table.event_id=:eventId
    """)
    suspend fun getAllTagsOfEvent(eventId: Long): List<Tag>

    @Query("""
        SELECT note_table.* 
        FROM note_table 
        JOIN note_table_fts ON note_table.note_id=note_table_fts.note_id
        JOIN event_note_table ON event_note_table.note_id=note_table.note_id
        WHERE note_table_fts MATCH :query 
        AND event_note_table.event_id=:eventId 
        AND (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(:lastNoteUpdateDate), :lastNoteId) 
        ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC LIMIT :noteAmt
    """)
    suspend fun getSearchedNotesOfEventPaginated(
        eventId: Long,
        query: String,
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long,
        noteAmt: Int
    ): List<Note>

    @RawQuery
    suspend fun searchEvents(dynamicQuery: SupportSQLiteQuery): List<Event>
}