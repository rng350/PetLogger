package com.hfad.petlogger.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hfad.petlogger.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Transaction
    suspend fun insertPet(petToInsert: Pet): Pet {
        val rowId = insert(petToInsert)
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

    @Query("SELECT * FROM pet_table WHERE pet_id=:petId")
    suspend fun getAsync(petId: Long): Pet?

    @Query("SELECT * FROM pet_table")
    suspend fun getAll(): MutableList<Pet>

    @Query("SELECT event_table.event_id AS event_id, event_title, event_details, event_date " +
            "FROM event_table, event_pet_table " +
            "WHERE event_pet_table.pet_id=:petId " +
            "AND event_table.event_id=event_pet_table.event_id " +
            "ORDER BY event_date")
    suspend fun getEventsOfPet(petId: Long): MutableList<Event>

    @Query("SELECT photo_table.photo_id, photo_date, photo_filesize, photo_width, photo_height, photo_uri, photo_filename, photo_title " +
            "FROM photo_table LEFT JOIN pet_profile_photo_table " +
            "ON photo_table.photo_id = pet_profile_photo_table.photo_id " +
            "WHERE pet_profile_photo_table.pet_id=:petID " +
            "LIMIT 1")
    suspend fun getPetProfilePhoto(petID: Long): Photo?

    //TODO: add "ORDER BY weight_date DESCENDING"
    @Query("SELECT * FROM weight_table WHERE weight_pet_id=:petId ORDER BY weight_datetime DESC")
    suspend fun getWeightsOfPet(petId: Long): MutableList<Weight>

    @Insert
    suspend fun insert(petEvent: EventPet)

    @Delete
    suspend fun delete(petEvent: EventPet)

    @Delete
    suspend fun delete(petEvents: MutableList<EventPet>)

    @Query("SELECT * FROM pet_table")
    suspend fun getAllPetsWithProfilePhotos(): List<PetWithProfilePic>

    @Query("SELECT * FROM pet_table WHERE pet_id=:petID")
    suspend fun getPetWithProfilePic(petID: Long): PetWithProfilePic

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPetPhoto(petPhoto: PetPhoto)

    @Update
    suspend fun updatePetPhoto(petPhoto: PetPhoto)

    @Delete
    suspend fun deletePetPhoto(petPhoto: PetPhoto)

    @Query("SELECT * FROM pet_table WHERE rowid = :rowId")
    suspend fun getPetFromRow(rowId: Long): Pet

    @Query("SELECT photo_table.photo_id, photo_date, photo_filesize, photo_width, photo_height, photo_uri, photo_filename, photo_title " +
            "FROM photo_table LEFT JOIN pet_photo_table " +
            "ON photo_table.photo_id = pet_photo_table.photo_id " +
            "WHERE pet_photo_table.pet_id=:petId " +
            "ORDER BY photo_date DESC")
    fun getPetPhotos(petId: Long): Flow<List<Photo>>


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
}