package com.hfad.petlogger.dao

import androidx.room.*
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetPhoto
import com.hfad.petlogger.entities.PetProfilePhoto
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoEvent
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entities.WeightDetails
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo)

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photo_table WHERE photo_id=:photoId LIMIT 1")
    suspend fun getPhoto(photoId: Long): Photo

    @Query("SELECT * FROM photo_table WHERE photo_id=:photoId LIMIT 1")
    suspend fun checkPhoto(photoId: Long): Photo?

    @Query("SELECT * FROM photo_table ORDER BY photo_date DESC")
    suspend fun getAllPhotos(): List<Photo>

    @Query("""
        SELECT * FROM photo_table 
        WHERE (datetime(photo_date), photo_id) < (datetime(:lastPhotoDate), :lastPhotoId) 
        ORDER BY datetime(photo_date) DESC, photo_id DESC LIMIT :amtLimit
    """)
    suspend fun getAllPhotosPaginated(lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photoEvent: PhotoEvent)

    @Update
    suspend fun update(photoEvent: PhotoEvent)

    @Delete
    suspend fun delete(photoEvent: PhotoEvent)

    @Delete
    suspend fun delete(photoEvents: List<PhotoEvent>)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun associate(petPhoto: PetPhoto)

    @Delete
    suspend fun dissociate(petPhoto: PetPhoto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(petProfilePhoto: PetProfilePhoto): Long

    @Update
    suspend fun update(petProfilePhoto: PetProfilePhoto)

    @Delete
    suspend fun delete(petProfilePhoto: PetProfilePhoto)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photoNote: PhotoNote)

    @Update
    suspend fun update(photoNote: PhotoNote)

    @Delete
    suspend fun delete(photoNote: PhotoNote)

    @Query("SELECT * FROM photo_table WHERE rowid = :rowId")
    suspend fun getPhotoFromRow(rowId: Long): Photo

    @Query("""        
        SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri 
        FROM pet_table 
        LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id 
        LEFT JOIN photo_table ON photo_table.photo_id=pet_profile_photo_table.photo_id 
        JOIN pet_photo_table ON pet_table.pet_id=pet_photo_table.pet_id  
        WHERE pet_photo_table.photo_id=:photoId
    """)
    fun getPetsOfPhoto(photoId: Long): List<PetWithProfilePic>

    @Query("""        
        SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri 
        FROM pet_table 
        LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id 
        LEFT JOIN photo_table ON photo_table.photo_id=pet_profile_photo_table.photo_id 
        JOIN pet_photo_table ON pet_table.pet_id=pet_photo_table.pet_id  
        WHERE pet_photo_table.photo_id=:photoId 
        AND pet_table.pet_id > :lastPetId 
        ORDER BY pet_table.pet_id ASC LIMIT :amtLimit
    """)
    suspend fun getPetsOfPhotoPaginated(photoId: Long, lastPetId: Long, amtLimit: Int): List<PetWithProfilePic>

    @Query("SELECT event_table.* " +
            "FROM event_table " +
            "LEFT JOIN photo_event_table " +
            "ON event_table.event_id=photo_event_table.event_id " +
            "WHERE photo_id=:photoId " +
            "ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC")
    suspend fun getEventsOfPhoto(photoId: Long): List<Event>

    @Query("SELECT * FROM photo_table")
    fun getAllPhotosAsFlow(): Flow<List<Photo>>

    @Query("SELECT event_table.* " +
            "FROM photo_event_table LEFT JOIN event_table " +
            "ON event_table.event_id=photo_event_table.event_id " +
            "WHERE photo_id=:photoId " +
            "AND (datetime(event_table.event_date), event_table.event_id) < (datetime(:lastEventDate), :lastEventId) " +
            "ORDER BY datetime(event_table.event_date) DESC, event_table.event_id DESC LIMIT :eventAmt")
    suspend fun getEventsOfPhotoPaginated(photoId: Long, lastEventDate: OffsetDateTime, lastEventId: Long, eventAmt: Int): List<Event>
    
    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN photo_note_table " +
            "ON note_table.note_id=photo_note_table.note_id " +
            "WHERE photo_note_table.photo_id=:photoId " +
            "AND (datetime(note_last_updated), note_table.note_id) < (:lastNoteEditedDate, :lastNoteId) " +
            "ORDER BY datetime(note_last_updated) DESC, note_table.note_id DESC LIMIT :notesAmt ")
    suspend fun getNotesOfPhotoPaginated(photoId: Long, lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, notesAmt: Int): List<Note>

    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN photo_note_table " +
            "ON note_table.note_id=photo_note_table.note_id " +
            "WHERE photo_note_table.photo_id=:photoId " +
            "ORDER BY datetime(note_last_updated) DESC, note_table.note_id DESC")
    suspend fun getNotesOfPhoto(photoId: Long): List<Note>
}