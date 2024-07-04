package com.hfad.petlogger.dao

import androidx.room.*
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightDetails
import com.hfad.petlogger.entities.WeightWithPetName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weight: Weight)

    @Insert
    suspend fun insert(weights: List<Weight>)

    @Update
    suspend fun update(weight: Weight)

    @Delete
    suspend fun delete(weight: Weight)

    @Delete
    suspend fun delete(weights: List<Weight>)

    @Query("SELECT * FROM weight_table WHERE weight_id=:weightId")
    suspend fun get(weightId: Long): Weight

    @Query("SELECT * FROM weight_table ORDER BY weight_datetime DESC")
    suspend fun getAll(): MutableList<Weight>

    @Query("SELECT * FROM weight_table " +
            "WHERE weight_pet_id=:petId " +
            "AND weight_datetime < :weightDateTimeInString " +
            "ORDER BY weight_datetime DESC " +
            "LIMIT 1")
    fun getPreviousWeight(petId: Long, weightDateTimeInString: String): Flow<Weight?>

    @Query("SELECT * FROM weight_table WHERE weight_id = :weightID")
    suspend fun getWeightDetails(weightID: Long): WeightDetails

    @Query("SELECT * FROM weight_table")
    suspend fun getAllWeightDetails(): List<WeightDetails>

    @Query("SELECT * FROM weight_table")
    fun getAllAsFlow(): Flow<WeightDetails>

    @Query("SELECT * FROM weight_table WHERE weight_id = :weightId LIMIT 1")
    fun getWeightAsFlow(weightId: Long): Flow<Weight>

    @Query("SELECT pet_table.*, photo_table.* " +
            "FROM pet_table " +
            "LEFT JOIN weight_table " +
            "ON pet_table.pet_id=weight_table.weight_pet_id " +
            "LEFT JOIN pet_profile_photo_table " +
            "ON pet_profile_photo_table.pet_id=pet_table.pet_id " +
            "LEFT JOIN photo_table " +
            "ON pet_profile_photo_table.photo_id=photo_table.photo_id " +
            "WHERE weight_table.weight_id=:weightId " +
            "LIMIT 1")
    fun getPetWithProfilePicOfWeight(weightId: Long): Flow<PetWithProfilePic>

    // TODO: there should be a faster query than this
    @Query("SELECT weight_table.* FROM weight_table " +
            "WHERE weight_pet_id IN (SELECT weight_pet_id FROM weight_table WHERE weight_id=:weightId LIMIT 1) " +
            "AND weight_datetime IN " +
            "(SELECT MAX(weight_datetime) FROM weight_table " +
            "WHERE weight_pet_id IN (SELECT weight_pet_id FROM weight_table WHERE weight_id =:weightId LIMIT 1) " +
            "AND weight_datetime < (SELECT weight_datetime FROM weight_table WHERE weight_id =:weightId LIMIT 1))")
    fun getPreviousWeight(weightId: Long): Flow<Weight>
}