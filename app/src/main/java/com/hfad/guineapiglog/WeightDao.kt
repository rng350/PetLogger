package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weight: Weight)

    @Update
    suspend fun update(weight: Weight)

    @Delete
    suspend fun delete(weight: Weight)

    @Query("SELECT * FROM weight_table WHERE weight_id=:weightId")
    suspend fun get(weightId: Long): Weight

    //TODO: add "ORDER BY weight_date DESCENDING"
    @Query("SELECT * FROM weight_table WHERE weight_pet_id=:petId ORDER BY weight_datetime")
    suspend fun getPetWeights(petId: Long): MutableList<Weight>

    @Query("SELECT * FROM weight_table")
    suspend fun getAll(): MutableList<Weight>
}