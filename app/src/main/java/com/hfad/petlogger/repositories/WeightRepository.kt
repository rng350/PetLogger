package com.hfad.petlogger.repositories

import android.util.Log
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightDetails
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Converter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class WeightRepository(database: PetLoggerDatabase) {
    private val weightDao: WeightDao = database.weightDao
    suspend fun get(weightId: Long): Weight = withContext(Dispatchers.IO) {
        weightDao.get(weightId)
    }

    fun getWeight(weightId: Long): Flow<Weight> {
        return weightDao.getWeightAsFlow(weightId)
    }

    fun getPetOfWeight(weightId: Long): Flow<PetWithProfilePic> {
        return weightDao.getPetWithProfilePicOfWeight(weightId).flowOn(Dispatchers.IO)
    }

    fun getAllWeights(): Flow<WeightDetails> {
        return weightDao.getAllAsFlow().flowOn(Dispatchers.IO)
    }

    suspend fun getAllWithPetNames(): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        val allWeightDetails = weightDao.getAllWeightDetails()
        val sorted = allWeightDetails.sortedByDescending { it.weight.weightDateTime }
        sorted.map {
            WeightWithPetName(it.weight, it.assocPet.petName)
        }
    }

    fun getPreviousWeight(weightId: Long, weightDateTime: OffsetDateTime): Flow<Weight?> {
        return weightDao.getPreviousWeight(weightId, Converter.fromOffsetDateTime(weightDateTime)!!)
    }

    fun getPreviousWeight(weightId: Long): Flow<Weight> {
        return weightDao.getPreviousWeight(weightId).onEach { Log.d("weightRep", "getPreviousWeight: ${it}") }.flowOn(Dispatchers.IO)
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