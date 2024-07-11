package com.hfad.petlogger.repositories

import android.util.Log
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightDetails
import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Converter
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class WeightRepository(database: PetLoggerDatabase) {
    private val weightDao: WeightDao = database.weightDao
    suspend fun get(weightId: Long): Weight = withContext(Dispatchers.IO) {
        weightDao.get(weightId)
    }

    suspend fun getWeightDetails(weightId: Long): WeightDetails = withContext(Dispatchers.IO) {
        weightDao.getWeightDetails(weightId)
    }

    fun getWeight(weightId: Long): Flow<Weight> {
        return weightDao.getWeightAsFlow(weightId).onEach { Log.d("getWeight", "Got Weight: ${it}") }
    }

    fun getPetOfWeight(weightId: Long): Flow<PetWithProfilePic> {
        return weightDao.getPetWithProfilePicOfWeight(weightId).flowOn(Dispatchers.IO)
    }

    fun getAllWeightsForDisplay(): Flow<List<WeightForList>> {
        val getDateDisplay = GetDateDisplayUseCase()
        val getTimeDisplay = GetTimeDisplayUseCase()
        return weightDao
            .getWeightsWithPetNameAndPhoto()
            .map { list ->
                list.sortedByDescending { weight ->
                    weight.weightDateTime
                }.map { weight ->
                    WeightForList(
                        weightId = weight.weightId,
                        weightGramsAmt = "${weight.weightGramsAmt}g",
                        weightDate = getDateDisplay(weight.weightDateTime),
                        weightTime = getTimeDisplay(weight.weightDateTime),
                        weightPetName = weight.weightPetName,
                        weightPetPhotoUri = weight.weightPetPhotoUri)
                }
            }.flowOn(Dispatchers.IO)
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
        return weightDao.getPreviousWeight(weightId).flowOn(Dispatchers.IO)
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