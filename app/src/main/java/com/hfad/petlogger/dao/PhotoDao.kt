package com.hfad.petlogger.dao

import androidx.room.*
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetProfilePhoto
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoEvent
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entities.WeightDetails
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photoEvent: PhotoEvent)

    @Update
    suspend fun update(photoEvent: PhotoEvent)

    @Delete
    suspend fun delete(photoEvent: PhotoEvent)

    @Delete
    suspend fun delete(photoEvents: List<PhotoEvent>)

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

    @Transaction
    @Query("SELECT pet_table.* " +
            "FROM pet_table LEFT JOIN pet_photo_table " +
            "ON pet_table.pet_id=pet_photo_table.pet_id " +
            "WHERE pet_photo_table.photo_id=:photoId")
    fun getPetsOfPhoto(photoId: Long): Flow<List<PetWithProfilePic>>

    @Query("SELECT event_table.* " +
            "FROM event_table LEFT JOIN photo_event_table " +
            "ON event_table.event_id=photo_event_table.event_id " +
            "WHERE photo_id=:photoId")
    fun getEventsOfPhoto(photoId: Long): Flow<List<Event>>

    @Query("SELECT * FROM photo_table")
    fun getAllPhotosAsFlow(): Flow<List<Photo>>
}