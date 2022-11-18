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

    @Query("SELECT * FROM weight_table")
    suspend fun getAll(): MutableList<Weight>
}