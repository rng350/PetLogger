package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WeightDao {
    @Insert
    fun insert(weight: Weight)

    @Update
    fun update(weight: Weight)

    @Delete
    fun delete(weight: Weight)

    @Query("SELECT * FROM weight_table WHERE weight_id=:weightId")
    fun get(weightId: Long): LiveData<Weight>

    /*//TODO: add "ORDER BY weight_date DESCENDING"
    @Query("SELECT * FROM weight_table WHERE weight_pet_id=:petId ORDER BY weight_datetime")
    fun getPetWeights(petId: Long): LiveData<List<Weight>>

    @Query("SELECT MAX(weight_datetime) FROM weight_table WHERE weight_pet_id=:petId")
    fun getMostRecentWeight(petId: Long): LiveData<Weight>*/
}