package com.hfad.petlogger.weights

import android.util.Log
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.notes.NoteDao
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.common.associationentities.WeightNote
import com.hfad.petlogger.common.util.Constants.Companion.newTagPlaceholderId
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.tags.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class WeightRepository(private val database: PetLoggerDatabase) {
    private val weightDao: WeightDao = database.weightDao
    private val noteDao: NoteDao = database.noteDao
    suspend fun get(weightId: Long): Weight = withContext(Dispatchers.IO) {
        weightDao.get(weightId)
    }

    suspend fun getWeightDetails(weightId: Long): WeightDetails = withContext(Dispatchers.IO) {
        weightDao.getWeightDetails(weightId)
    }

    suspend fun getWeightFullDetails(weightId: Long): WeightFullDetailsState = withContext(Dispatchers.IO) {
        weightDao.getFullWeightDetails(weightId).toState()
    }

    suspend fun getAllWithPetNames(): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        val allWeightDetails = weightDao.getAllWeightDetails()
        val sorted = allWeightDetails.sortedByDescending { it.weight.weightDateTime }
        sorted.map {
            WeightWithPetName(it.weight, it.assocPet.petName)
        }
    }

    suspend fun addWeight(
        weight: Weight,
        notes: List<Note> = listOf<Note>(),
        tags: List<Tag> = listOf<Tag>()
    ): Weight = withContext(Dispatchers.IO) {
        val weightAdded = async {
            weightDao.addWeight(weight)
        }.await()

        val notesAdded = async {
            noteDao.attachWeights(notes.map { note -> WeightNote(weightId=weightAdded.id, noteId=note.id) })
        }
        val tagRepository = TagRepository(database)
        val tagsAdded = tags.map { tag ->
            async {
                attachWeightToTag(tagRepository, weightAdded.id, tag)
            }
        }
        notesAdded.await()
        tagsAdded.awaitAll()
        weightAdded
    }

    suspend fun insert(weight: WeightWithPetName) = withContext(Dispatchers.IO) {
        weightDao.insert(weight.weight)
    }

    suspend fun update(
        weight: Weight,
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>(),
        tagsToAdd: List<Tag> = listOf<Tag>(),
        tagsToRemove: List<Tag> = listOf<Tag>()
    )
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
        val notesUpdated = notesToUpdate.map { note ->
            async {
                noteDao.update(note)
            }
        }
        val tagRepository = TagRepository(database)
        val tagsAttached = tagsToAdd.map { tag ->
            async {
                attachWeightToTag(tagRepository, weight.id, tag)
            }
        }
        val tagsDetached = tagsToRemove.map { tag ->
            async {
                tagRepository.detachWeightFromTag(weight.id, tag)
            }
        }
        weightUpdated.await()
        notesAttached.await()
        notesDetached.await()
        notesUpdated.awaitAll()
        tagsAttached.awaitAll()
        tagsDetached.awaitAll()
    }

    suspend fun update(weight: WeightWithPetName) {

    }

    suspend fun delete(weight: Weight) {
        weightDao.delete(weight)
    }

    suspend fun delete(weight: WeightWithPetName) {
        weightDao.delete(weight.weight)
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

    private suspend fun attachWeightToTag(tagRepository: TagRepository, weightId: Long, tag: Tag) {
        if (tag.tagId == newTagPlaceholderId) {
            tagRepository.attachWeightToNewTag(weightId, tag)
        } else tagRepository.attachWeightToExistingTag(weightId, tag)
    }

    suspend fun getTagsOfWeight(weightId: Long): List<Tag> = withContext(Dispatchers.IO) {
        weightDao.getTagsOfWeight(weightId)
    }

    suspend fun getTagsOfWeightAlphabeticalOrder(weightId: Long): List<Tag> = withContext(Dispatchers.IO) {
        weightDao.getTagsOfWeightAlphabeticalOrder(weightId)
    }
}