package com.hfad.petlogger.repositories

import android.util.Log
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightWithPetName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class WeightRepository(database: PetLoggerDatabase) {
    private val weightDao: WeightDao = database.weightDao
    private val petDao: PetDao = database.petDao
    suspend fun get(weightId: Long): Weight = withContext(Dispatchers.IO) {
        weightDao.get(weightId)
    }

    suspend fun getAll() {
    }

    suspend fun getAllWithPetNames(): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        val allWeightDetails = weightDao.getAllWeightDetails()
        val sorted = allWeightDetails.sortedByDescending { it.weight.weightDateTime }
        sorted.map {
            WeightWithPetName(it.weight, it.assocPet.petName)
        }
    }

    suspend fun insert(weight: Weight) = withContext(Dispatchers.IO) {
        weightDao.insert(weight)
    }

    suspend fun insert(weight: WeightWithPetName) = withContext(Dispatchers.IO) {
        insert(weight.weight)
    }

    suspend fun update(weight: Weight) {

    }

    suspend fun update(weight: WeightWithPetName) {

    }

    suspend fun delete(weight: Weight) {

    }

    suspend fun delete(weight: WeightWithPetName) {

    }
}