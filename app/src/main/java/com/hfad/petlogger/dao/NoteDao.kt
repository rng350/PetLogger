package com.hfad.petlogger.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetNote
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entities.WeightDetails
import com.hfad.petlogger.entities.WeightNote
import com.hfad.petlogger.entities.WeightWithPetName
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table WHERE note_id=:noteId")
    suspend fun get(noteId: Long): Note

    @Query("SELECT * FROM note_table ORDER BY note_last_updated DESC")
    suspend fun getAll(): List<Note>

    @Insert
    suspend fun insert(note: Note): Long

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

    @Transaction
    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN pet_note_table " +
            "WHERE pet_table.pet_id = pet_note_table.pet_id AND :noteId = pet_note_table.note_id")
    fun getPetsWithProfilePicOfNoteAsFlow(noteId: Long): Flow<List<PetWithProfilePic>>

    @Query("SELECT event_table.* " +
            "FROM event_table LEFT JOIN event_note_table " +
            "ON event_table.event_id=event_note_table.event_id " +
            "WHERE event_note_table.note_id=:noteId " +
            "ORDER BY event_table.event_date DESC")
    suspend fun getEventsOfNote(noteId: Long): List<Event>

    @Transaction
    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN pet_note_table " +
            "ON pet_table.pet_id = pet_note_table.pet_id " +
            "WHERE pet_note_table.note_id = :noteId")
    suspend fun getPetsWithProfilePicOfNote(noteId: Long): List<PetWithProfilePic>

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
}