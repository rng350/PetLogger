package com.hfad.petlogger.pets

import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hfad.petlogger.common.associationentities.EventPet
import com.hfad.petlogger.common.associationentities.PetPhoto
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.PetWeightForDisplayFetched
import com.hfad.petlogger.weights.Weight
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface PetDao {
    @Transaction
    suspend fun insertPet(petToInsert: Pet): Pet {
        Log.d("petDao", "about to insert....")
        val rowId = insert(petToInsert)
        Log.d("petDao", "inserted....")
        return getPetFromRow(rowId)
    }

    @Insert
    suspend fun insert(pet: Pet): Long

    @Delete
    suspend fun delete(pet: Pet)

    @Update
    suspend fun update(pet: Pet)

    @Query("SELECT * FROM pet_table WHERE pet_id=:petId")
    suspend fun getPet(petId: Long): Pet

    @Query("SELECT pet_table.pet_id AS petId, " +
            "pet_table.pet_name AS petName, " +
            "pet_table.pet_dob AS petDOB, " +
            "pet_table.pet_species AS petSpecies, " +
            "pet_table.pet_breed AS petBreed, " +
            "pet_table.pet_sex AS petSex," +
            "photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "WHERE pet_table.pet_id=:petId LIMIT 1")
    suspend fun getPetDetails(petId: Long): PetDetails

    @Query("SELECT * FROM pet_table WHERE pet_id=:petId")
    suspend fun getAsync(petId: Long): Pet?

    @Query("SELECT * FROM pet_table")
    suspend fun getAll(): List<Pet>

    @Query("SELECT event_table.event_id AS event_id, event_title, event_details, event_date " +
            "FROM event_table, event_pet_table " +
            "WHERE event_pet_table.pet_id=:petId " +
            "AND event_table.event_id=event_pet_table.event_id " +
            "ORDER BY event_date")
    suspend fun getEventsOfPet(petId: Long): List<Event>

    @Query("SELECT event_table.event_id AS event_id, event_title, event_details, event_date " +
            "FROM event_pet_table INNER JOIN event_table " +
            "ON event_table.event_id = event_pet_table.event_id " +
            "WHERE event_pet_table.pet_id=:petId " +
            "AND (datetime(event_table.event_date), event_table.event_id) < (datetime(:lastEventDate), :lastEventId) " +
            "ORDER BY datetime(event_date) DESC, event_id DESC LIMIT :amtLimit")
    suspend fun getEventsOfPetPaginated(petId: Long, lastEventDate: OffsetDateTime, lastEventId: Long, amtLimit: Int): List<Event>

    @Query("SELECT photo_table.photo_id, photo_date, photo_filesize, photo_width, photo_height, photo_uri, photo_filename, photo_title " +
            "FROM photo_table LEFT JOIN pet_profile_photo_table " +
            "ON photo_table.photo_id = pet_profile_photo_table.photo_id " +
            "WHERE pet_profile_photo_table.pet_id=:petID " +
            "LIMIT 1")
    suspend fun getPetProfilePhoto(petID: Long): Photo?

    //TODO: add "ORDER BY weight_date DESCENDING"
    @Query("SELECT * FROM weight_table WHERE weight_pet_id=:petId ORDER BY weight_datetime DESC")
    suspend fun getWeightsOfPet(petId: Long): List<Weight>

    @Query("""
            SELECT wt_1.weight_id AS weightId, wt_1.weight_grams AS weightGramsAmt, wt_1.weight_datetime AS weightDateTime, 
                (SELECT wt_2.weight_grams  
                FROM weight_table wt_2  
                WHERE wt_2.weight_pet_id=:petId 
                AND (datetime(wt_2.weight_datetime), wt_2.weight_id) < (datetime(wt_1.weight_datetime), wt_1.weight_id) 
                ORDER BY datetime(wt_2.weight_datetime) DESC, wt_2.weight_id DESC LIMIT 1
                ) AS prevWeightGramsAmt  
            FROM weight_table wt_1  
            WHERE wt_1.weight_pet_id=:petId  
            AND (datetime(wt_1.weight_datetime), wt_1.weight_id) < (datetime(:lastWeightDate), :lastWeightId)  
            ORDER BY datetime(wt_1.weight_datetime) DESC, wt_1.weight_id DESC LIMIT :amtLimit
            """)
    suspend fun getWeightsOfPetPaginated(petId: Long, lastWeightDate: OffsetDateTime, lastWeightId: Long, amtLimit: Int): List<PetWeightForDisplayFetched>

    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN pet_note_table " +
            "ON note_table.note_id=pet_note_table.note_id " +
            "WHERE pet_note_table.pet_id=:petId " +
            "AND (datetime(note_last_updated), note_table.note_id) < (datetime(:lastNoteEditedDate), :lastNoteId) " +
            "ORDER BY datetime(note_last_updated) DESC, note_table.note_id DESC LIMIT :amtLimit")
    suspend fun getNotesOfPetPaginated(petId: Long, lastNoteEditedDate: OffsetDateTime, lastNoteId: Long, amtLimit: Int): List<Note>

    @Insert
    suspend fun insert(petEvent: EventPet)

    @Insert
    suspend fun insert(petEvents: List<EventPet>)

    @Delete
    suspend fun delete(petEvent: EventPet)

    @Delete
    suspend fun delete(petEvents: List<EventPet>)

    @Query("SELECT pet_table.pet_name AS petName, pet_table.pet_id AS petId, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "ORDER BY pet_table.pet_id ASC")
    suspend fun getAllPetsWithProfilePhotos(): List<PetWithProfilePic>

    @Query("SELECT pet_table.pet_id AS petId, pet_table.pet_name AS petName, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "WHERE petId > :lastPetId " +
            "ORDER BY petId ASC LIMIT :amtLimit")
    suspend fun getAllPetsWithProfilePhotosPaginated(lastPetId: Long, amtLimit: Int): List<PetWithProfilePic>

    @Query("SELECT pet_table.pet_name AS petName, pet_table.pet_id AS petId, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "WHERE pet_table.pet_id=:petID")
    suspend fun getPetWithProfilePic(petID: Long): PetWithProfilePic

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPetPhoto(petPhoto: PetPhoto)

    @Update
    suspend fun updatePetPhoto(petPhoto: PetPhoto)

    @Delete
    suspend fun deletePetPhoto(petPhoto: PetPhoto)

    @Delete
    suspend fun deletePetPhotos(petPhotos: List<PetPhoto>)

    @Query("SELECT * FROM pet_table WHERE rowid = :rowId")
    suspend fun getPetFromRow(rowId: Long): Pet

    @Query("SELECT photo_table.photo_id, photo_date, photo_filesize, photo_width, photo_height, photo_uri, photo_filename, photo_title " +
            "FROM photo_table LEFT JOIN pet_photo_table " +
            "ON photo_table.photo_id = pet_photo_table.photo_id " +
            "WHERE pet_photo_table.pet_id=:petId " +
            "ORDER BY photo_date DESC")
    fun getPetPhotos(petId: Long): Flow<List<Photo>>

    @Query("SELECT photo_table.photo_id, photo_date, photo_filesize, photo_width, photo_height, photo_uri, photo_filename, photo_title " +
            "FROM photo_table INNER JOIN pet_photo_table " +
            "ON photo_table.photo_id = pet_photo_table.photo_id " +
            "WHERE pet_photo_table.pet_id=:petId " +
            "AND (datetime(photo_date), photo_table.photo_id) < (datetime(:lastPhotoDate), :lastPhotoId) " +
            "ORDER BY datetime(photo_date) DESC, photo_table.photo_id DESC LIMIT :amtLimit")
    suspend fun getPhotosOfPetPaginated(petId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo>

    @Query("SELECT photo_table.photo_id, photo_date, photo_filesize, photo_width, photo_height, photo_uri, photo_filename, photo_title " +
            "FROM photo_table LEFT JOIN pet_photo_table " +
            "ON photo_table.photo_id = pet_photo_table.photo_id " +
            "WHERE pet_photo_table.pet_id=:petId " +
            "ORDER BY photo_date DESC")
    suspend fun getPetPhotosAsList(petId: Long): List<Photo>

    @Query("SELECT weight_id, weight_pet_id, weight_datetime, weight_notes, weight_grams " +
            "FROM weight_table WHERE weight_pet_id = :petId " +
            "ORDER BY weight_datetime DESC")
    fun getPetWeights(petId: Long): Flow<List<Weight>>

    @Query("SELECT event_table.event_id, event_title, event_date, event_details FROM event_table " +
            "LEFT JOIN event_pet_table ON event_table.event_id = event_pet_table.event_id " +
            "WHERE event_pet_table.pet_id = :petId " +
            "ORDER BY event_date DESC")
    fun getPetEvents(petId: Long): Flow<List<Event>>

    @Query("SELECT pet_table.pet_name AS petName, pet_table.pet_id AS petId, photo_table.photo_uri AS petProfilePicUri " +
            "FROM pet_table " +
            "LEFT JOIN pet_profile_photo_table ON pet_table.pet_id=pet_profile_photo_table.pet_id " +
            "LEFT JOIN photo_table ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "ORDER BY pet_table.pet_id ASC")
    fun getAllPetsWithProfilePhotosAsFlow(): Flow<List<PetWithProfilePic>>

    @Query("SELECT note_table.* " +
            "FROM note_table INNER JOIN pet_note_table " +
            "ON note_table.note_id=pet_note_table.note_id " +
            "WHERE pet_note_table.pet_id=:petId " +
            "ORDER BY datetime(note_last_updated) DESC, note_table.note_id DESC")
    fun getNotesOfPet(petId: Long): List<Note>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN pet_tag_table  
        ON pet_tag_table.tag_id=tag_table.tag_id 
        WHERE pet_tag_table.pet_id=:petId
    """)
    suspend fun getAllTagsOfPet(petId: Long): List<Tag>

    @Query("""
        SELECT tag_table.* 
        FROM tag_table LEFT JOIN pet_tag_table  
        ON pet_tag_table.tag_id=tag_table.tag_id 
        WHERE pet_tag_table.pet_id=:petId 
        ORDER BY tag_table.tag_name ASC
    """)
    suspend fun getAllTagsOfPetAlphabeticalOrder(petId: Long): List<Tag>

    @Query("""
        SELECT note_table.* 
        FROM note_table 
        JOIN note_table_fts ON note_table.note_id=note_table_fts.note_id
        JOIN pet_note_table ON pet_note_table.note_id=note_table.note_id
        WHERE note_table_fts MATCH :query 
        AND pet_note_table.pet_id=:petId 
        AND (datetime(note_table.note_last_updated), note_table.note_id) < (datetime(:lastNoteUpdateDate), :lastNoteId) 
        ORDER BY datetime(note_table.note_last_updated) DESC, note_table.note_id DESC LIMIT :noteAmt
    """)
    suspend fun getSearchedNotesOfPetPaginated(
        petId: Long,
        query: String,
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long,
        noteAmt: Int
    ): List<Note>

    @RawQuery
    suspend fun searchPets(query: SimpleSQLiteQuery): List<PetWithProfilePic>

    @RawQuery
    suspend fun searchPetIds(query: SimpleSQLiteQuery): List<Long>
}