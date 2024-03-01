package com.hfad.petlogger.dao

import androidx.room.*
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightDetails
import java.time.OffsetDateTime

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weight: Weight)

    @Update
    suspend fun update(weight: Weight)

    @Delete
    suspend fun delete(weight: Weight)

    @Delete
    suspend fun delete(weights: MutableList<Weight>)

    @Query("SELECT * FROM weight_table WHERE weight_id=:weightId")
    suspend fun get(weightId: Long): Weight

    @Query("SELECT * FROM weight_table")
    suspend fun getAll(): MutableList<Weight>

    @Query("SELECT * FROM weight_table " +
            "WHERE weight_pet_id=:petId " +
            "AND weight_datetime < :weightDateTimeInString " +
            "ORDER BY weight_datetime DESC " +
            "LIMIT 1")
    suspend fun getPreviousWeight(petId: Long, weightDateTimeInString: String): Weight?

    @Query("SELECT * FROM weight_table WHERE weight_id = :weightID")
    suspend fun getWeightDetails(weightID: Long): WeightDetails
}