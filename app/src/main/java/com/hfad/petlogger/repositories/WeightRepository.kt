package com.hfad.petlogger.repositories

import android.util.Log
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.NoteDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightDetails
import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.entities.WeightForListFetched
import com.hfad.petlogger.entities.WeightFullDetailsFetched
import com.hfad.petlogger.entities.WeightFullDetailsState
import com.hfad.petlogger.entities.WeightNote
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Converter
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class WeightRepository(database: PetLoggerDatabase) {
    private val weightDao: WeightDao = database.weightDao
    private val noteDao: NoteDao = database.noteDao
    suspend fun get(weightId: Long): Weight = withContext(Dispatchers.IO) {
        weightDao.get(weightId)
    }

    suspend fun getWeightDetails(weightId: Long): WeightDetails = withContext(Dispatchers.IO) {
        weightDao.getWeightDetails(weightId)
    }

    fun getWeight(weightId: Long): Flow<Weight> {
        return weightDao.getWeightAsFlow(weightId).onEach { Log.d("getWeight", "Got Weight: ${it}") }
    }

    suspend fun getWeightFullDetails(weightId: Long): WeightFullDetailsState = withContext(Dispatchers.IO) {
        weightDao.getFullWeightDetails(weightId).toState()
    }

    fun getPetOfWeight(weightId: Long): Flow<PetWithProfilePic> {
        return weightDao.getPetWithProfilePicOfWeight(weightId).flowOn(Dispatchers.IO)
    }

    fun getAllWeightsForDisplay(): Flow<List<WeightForList>> {
        return weightDao
            .getWeightsWithPetNameAndPhoto()
            .map { list ->
                list.sortedByDescending { weight ->
                    weight.weightDateTime
                }.map { weight ->
                    weight.toWeightForList()
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

    suspend fun addWeight(weight: Weight, notes: List<Note> = listOf<Note>()): Weight = withContext(Dispatchers.IO) {
        val weightAdded = async {
            weightDao.addWeight(weight)
        }.await()

        val notesAdded = async {
            noteDao.attachWeights(notes.map { note -> WeightNote(weightId=weightAdded.id, noteId=note.id) })
        }.await()

        weightAdded
    }

    suspend fun insert(weight: WeightWithPetName) = withContext(Dispatchers.IO) {
        weightDao.insert(weight.weight)
    }

    suspend fun update(
        weight: Weight,
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>())
    = withContext(Dispatchers.IO) {
        val weightUpdated = async {
            weightDao.update(weight)
        }
        val notesAttached = async {
            noteDao.attachWeights(notesToAdd.map { note -> WeightNote(weightId=weight.id, noteId = note.id) })
        }
        val notesDetached = async {
            noteDao.detachWeights(notesToRemove.map { note -> WeightNote(weightId = weight.id, noteId = note.id) })
        }
        notesToUpdate.map { note ->
            async {
                noteDao.update(note)
            }
        }.awaitAll()
        notesAttached.await()
        notesDetached.await()
        weightUpdated.await()
    }

    suspend fun update(weight: WeightWithPetName) {

    }

    suspend fun delete(weight: Weight) {

    }

    suspend fun delete(weight: WeightWithPetName) {

    }

    suspend fun getNotesOfWeightPaginated(
        weightId: Long,
        lastNoteEditedDate: OffsetDateTime,
        lastNoteId: Long,
        amtLimit: Int
    ): List<Note> = withContext(Dispatchers.IO) {
        weightDao.getNotesOfWeightPaginated(weightId, lastNoteEditedDate, lastNoteId, amtLimit)
    }

    suspend fun getAllWeightsPaginated(lastWeightDateTime: OffsetDateTime, lastWeightId: Long, weightsAmt: Int): List<WeightForListFetched> = withContext(Dispatchers.IO) {
        weightDao.getAllWeightsPaginated(lastWeightDateTime, lastWeightId, weightsAmt)
    }

    suspend fun getNotesOfWeight(weightId: Long): List<Note> = withContext(Dispatchers.IO) {
        weightDao.getNotesOfWeight(weightId)
    }
}