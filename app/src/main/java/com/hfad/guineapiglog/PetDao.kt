package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PetDao {
    @Insert
    suspend fun insert(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Update
    suspend fun update(pet: Pet)

    @Query("SELECT * FROM pet_table WHERE pet_id=:petId")
    fun get(petId: Long): LiveData<Pet>

    @Query("SELECT * FROM pet_table")
    fun getAll(): LiveData<List<Pet>>
}