package com.hfad.petlogger.notes

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.common.associationentities.EventNote
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.common.associationentities.PetNote
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.associationentities.PhotoNote
import com.hfad.petlogger.weights.WeightDetails
import com.hfad.petlogger.weights.WeightForListFetched
import com.hfad.petlogger.common.associationentities.WeightNote
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table WHERE note_id=:noteId")
    suspend fun get(noteId: Long): Note

    @Query("SELECT * FROM note_table ORDER BY note_last_updated DESC")
    suspend fun getAll(): List<Note>

    @Query("""
        SELECT * FROM note_table 
        WHERE (datetime(note_last_updated), note_id) < (datetime(:lastNoteEditedDate), :lastNoteId) 
        ORDER BY datetime(note_last_updated) DESC, note_id DESC LIMIT :amtLimit
    """)
    suspend fun getAllNotesPaginated(lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, amtLimit: Int): List<Note>

    @Query("""
        SELECT * FROM note_table 
        JOIN note_tag_table ON note_table.note_id=note_tag_table.note_id 
        WHERE (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(:lastNoteEditedDate), :lastNoteId) 
        AND note_tag_table.tag_id=:tagId
        ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC LIMIT :amtLimit
    """)
    suspend fun getAllNotesOfTagPaginated(tagId: Long, lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, amtLimit: Int): List<Note>

    @Transaction
    suspend fun insertNewNote(note: Note): Note {
        val rowId = insert(note)
        return getNoteFromRowId(rowId)
    }

    @Insert
    suspend fun insert(note: Note): Long

    @Query("SELECT * FROM note_table WHERE rowid = :rowId")
    suspend fun getNoteFromRowId(rowId: Long): Note

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN pet_note_table " +
            "WHERE pet_table.pet_id = pet_note_table.pet_id AND :noteId = pet_note_table.note_id")
    suspend fun getPetsOfNote(noteId: Long): List<Pet>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachPet(petNote: PetNote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachPets(petNotes: List<PetNote>)

    @Delete
    suspend fun detachPet(petNote: PetNote)
    @Delete
    suspend fun detachPets(petNotes: List<PetNote>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachEvent(eventNote: EventNote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachEvents(eventNotes: List<EventNote>)

    @Update
    suspend fun update(eventNote: EventNote)

    @Delete
    suspend fun detachEvent(eventNote: EventNote)
    @Delete
    suspend fun detachEvents(eventNotes: List<EventNote>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachWeight(weightNote: WeightNote)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachWeights(weightNotes: List<WeightNote>)

    @Update
    suspend fun update(weightNote: WeightNote)

    @Delete
    suspend fun detachWeight(weightNote: WeightNote)
    @Delete
    suspend fun detachWeights(weightNotes: List<WeightNote>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attachPhoto(photoNote: PhotoNote)

    @Update
    suspend fun update(photoNote: PhotoNote)

    @Delete
    suspend fun detachPhoto(photoNote: PhotoNote)
    @Delete
    suspend fun detachPhotos(photoNotes: List<PhotoNote>)

    @Query("SELECT photo_table.photo_id, photo_uri, photo_title, photo_filename, photo_date, photo_filesize, photo_width, photo_height " +
            "FROM photo_table LEFT JOIN photo_note_table " +
            "ON photo_note_table.photo_id = photo_table.photo_id " +
            "WHERE photo_note_table.note_id = :noteId")
    fun getPhotosOfNote(noteId: Long): Flow<List<Photo>>

    @Query("SELECT photo_table.photo_id, photo_uri, photo_title, photo_filename, photo_date, photo_filesize, photo_width, photo_height " +
            "FROM photo_table INNER JOIN photo_note_table " +
            "ON photo_note_table.photo_id = photo_table.photo_id " +
            "WHERE photo_note_table.note_id = :noteId " +
            "AND (datetime(photo_date), photo_table.photo_id) < (datetime(:lastPhotoDate), :lastPhotoId) " +
            "ORDER BY datetime(photo_date) DESC, photo_table.photo_id DESC LIMIT :amtLimit")
    suspend fun getPhotosOfNotePaginated(noteId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table " +
            "ON pet_table.pet_id = pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table " +
            "ON photo_table.photo_id = pet_profile_photo_table.photo_id " +
            "LEFT JOIN pet_note_table " +
            "ON pet_table.pet_id = pet_note_table.pet_id " +
            "LEFT JOIN note_table " +
            "ON note_table.note_id = pet_note_table.note_id " +
            "WHERE note_table.note_id = :noteId")
    fun getPetsWithProfilePicOfNoteAsFlow(noteId: Long): Flow<List<PetWithProfilePic>>

    @Query("SELECT event_table.* " +
            "FROM event_table LEFT JOIN event_note_table " +
            "ON event_table.event_id=event_note_table.event_id " +
            "WHERE event_note_table.note_id=:noteId " +
            "ORDER BY event_table.event_date DESC")
    suspend fun getEventsOfNote(noteId: Long): List<Event>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_note_table " +
            "LEFT JOIN pet_table ON pet_note_table.pet_id=pet_table.pet_id " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "WHERE pet_note_table.note_id = :noteId " +
            "ORDER BY pet_note_table.pet_id ASC")
    suspend fun getPetsWithProfilePicOfNote(noteId: Long): List<PetWithProfilePic>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM note_table " +
            "LEFT JOIN pet_note_table ON note_table.note_id=pet_note_table.note_id " +
            "LEFT JOIN pet_table ON pet_note_table.pet_id=pet_table.pet_id " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "WHERE note_table.note_id = :noteId " +
            "AND pet_table.pet_id > :lastPetId " +
            "ORDER BY pet_table.pet_id ASC LIMIT :amtLimit")
    suspend fun getPetsOfNotePaginated(noteId: Long, lastPetId: Long, amtLimit: Int): List<PetWithProfilePic>
    @Query("SELECT photo_table.photo_id, photo_uri, photo_title, photo_filename, photo_date, photo_filesize, photo_width, photo_height " +
            "FROM photo_table LEFT JOIN photo_note_table " +
            "ON photo_note_table.photo_id = photo_table.photo_id " +
            "WHERE photo_note_table.note_id = :noteId")
    suspend fun getPhotosOfNoteAsList(noteId: Long): List<Photo>

    @Transaction
    @Query("SELECT weight_table.* " +
            "FROM weight_table LEFT JOIN weight_note_table " +
            "ON weight_table.weight_id=weight_note_table.weight_id " +
            "WHERE weight_note_table.note_id=:noteId")
    suspend fun getWeightsOfNote(noteId: Long): List<WeightDetails>

    @Query("SELECT event_table.* " +
            "FROM event_table LEFT JOIN event_note_table " +
            "ON event_table.event_id=event_note_table.event_id " +
            "WHERE event_note_table.note_id=:noteId " +
            "ORDER BY event_table.event_date DESC")
    fun getEventsOfNoteAsFlow(noteId: Long): Flow<List<Event>>

    @Query("SELECT * FROM note_table")
    fun getAllNotesAsFlow(): Flow<List<Note>>

    @Query("SELECT event_table.* " +
            "FROM event_table INNER JOIN event_note_table " +
            "ON event_table.event_id=event_note_table.event_id " +
            "WHERE event_note_table.note_id=:noteId " +
            "AND (datetime(event_table.event_date), event_table.event_id) < (datetime(:lastEventDate), :lastEventId) " +
            "ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC LIMIT :amtLimit")
    suspend fun getEventsOfNotePaginated(noteId: Long, lastEventDate: OffsetDateTime, lastEventId: Long, amtLimit: Int): List<Event>

    @Query("""
        SELECT 
            wt_1.weight_id AS weightId, 
            wt_1.weight_datetime AS weightDateTime, 
            wt_1.weight_grams AS weightGramsAmt, 
            pet_table.pet_name AS weightPetName, 
            photo_table.photo_uri AS weightPetProfilePhotoUri,
            (
                SELECT wt_2.weight_grams 
                FROM weight_table wt_2 
                WHERE wt_2.weight_pet_id = wt_1.weight_pet_id 
                AND (datetime(wt_2.weight_datetime), wt_2.weight_id) < (datetime(wt_1.weight_datetime), wt_1.weight_id) 
                ORDER BY datetime(wt_2.weight_datetime) DESC, wt_2.weight_id DESC LIMIT 1
            ) AS prevWeightGramsAmt
        FROM weight_table wt_1
        LEFT JOIN pet_table ON wt_1.weight_pet_id=pet_table.pet_id 
        LEFT JOIN pet_profile_photo_table ON pet_profile_photo_table.pet_id=wt_1.weight_pet_id
        LEFT JOIN photo_table ON photo_table.photo_id=pet_profile_photo_table.photo_id 
        LEFT JOIN weight_note_table ON weight_note_table.note_id=wt_1.weight_id 
        WHERE weight_note_table.note_id=:noteId 
        AND (datetime(wt_1.weight_datetime), wt_1.weight_id) < (datetime(:lastWeightDateTime), :lastWeightId) 
        ORDER BY datetime(wt_1.weight_datetime) DESC, wt_1.weight_id DESC LIMIT :amtLimit
    """)
    suspend fun getWeightsOfNotePaginated(noteId: Long, lastWeightDateTime: OffsetDateTime, lastWeightId: Long, amtLimit: Int): List<WeightForListFetched>


    @Query("""
        SELECT note_table.* 
        FROM note_table JOIN note_table_fts 
        ON note_table.note_id=note_table_fts.note_id 
        WHERE note_table_fts MATCH :query 
        AND (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(:lastNoteUpdateDate), :lastNoteId) 
        ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC LIMIT :noteAmt
    """)
    suspend fun getSearchedNotesFromAllPaginated(query: String, lastNoteUpdateDate: OffsetDateTime, lastNoteId: Long, noteAmt: Int): List<Note>

    @RawQuery
    suspend fun searchNotes(query: SimpleSQLiteQuery): List<Note>

    @RawQuery
    suspend fun searchNoteIds(query: SimpleSQLiteQuery): List<Long>
}