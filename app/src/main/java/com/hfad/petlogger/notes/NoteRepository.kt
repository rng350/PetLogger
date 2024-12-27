package com.hfad.petlogger.notes

import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import com.hfad.petlogger.common.associationentities.EventNote
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.common.associationentities.PetNote
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.associationentities.PhotoNote
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.weights.WeightForListFetched
import com.hfad.petlogger.common.associationentities.WeightNote
import com.hfad.petlogger.weights.WeightWithPetName
import com.hfad.petlogger.common.util.Constants.Companion.newTagPlaceholderId
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.tags.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class NoteRepository(
    private val database: PetLoggerDatabase,
    private val mediaRepository: MediaRepository
) {
    private val noteDao = database.noteDao
    suspend fun getNote(noteId: Long): Note
    = withContext(Dispatchers.IO) {
        noteDao.get(noteId)
    }

    suspend fun getAllNotes(): List<Note> {
        return noteDao.getAll()
    }

    suspend fun insertNote(note: Note,
                           pets: List<Long> = listOf<Long>(),
                           events: List<Long> = listOf<Long>(),
                           weights: List<Long> = listOf<Long>(),
                           photos: List<Photo> = listOf<Photo>(),
                           tags: List<Tag> = listOf<Tag>()): Long
    = withContext(Dispatchers.IO) {

        val noteId = noteDao.insertNewNote(note).id

        val petsDeferred = pets.map {
            async {
                insertPetNote(noteId, it)
            }
        }
        val eventsDeferred = events.map {
            async {
                insertEventNote(noteId, it)
            }
        }
        val weightsDeferred = weights.map {
            async {
                insertWeightNote(noteId = noteId, weightId = it)
            }
        }
        val photosDeferred = photos.map {
            async {
                mediaRepository.addNewPhotoForNote(it, noteId)
            }
        }
        val tagRepository = TagRepository(database)
        val tagsDeferred = tags.map { tag ->
            async {
                attachTagToNote(tagRepository, noteId, tag)
            }
        }

        petsDeferred.awaitAll()
        eventsDeferred.awaitAll()
        weightsDeferred.awaitAll()
        photosDeferred.awaitAll()
        tagsDeferred.awaitAll()

        noteId
    }

    suspend fun updateNote(note: Note,
                           petsToAdd: List<Long> = listOf<Long>(),
                           petsToRemove: List<Long> = listOf<Long>(),
                           eventsToAdd: List<Long> = listOf<Long>(),
                           eventsToRemove: List<Long> = listOf<Long>(),
                           weightsToAdd: List<Weight> = listOf<Weight>(),
                           weightsToRemove: List<Weight> = listOf<Weight>(),
                           photosToAdd: List<Photo> = listOf<Photo>(),
                           photosToRemove: List<Photo> = listOf<Photo>(),
                           tagsToAdd: List<Tag> = listOf<Tag>(),
                           tagsToRemove: List<Tag> = listOf<Tag>())
    = withContext(Dispatchers.IO) {
        val noteUpdated = async {
            noteDao.update(note)
        }
        val petsAttached = async {
            noteDao.attachPets(petsToAdd.map{ petID -> PetNote(petId=petID, noteId=note.id) })
        }
        val petsDetached = async {
            noteDao.detachPets(petsToRemove.map{ petID -> PetNote(petId=petID, noteId=note.id) })
        }
        val eventsAttached = async {
            noteDao.attachEvents(eventsToAdd.map{ eventID -> EventNote(eventId = eventID, noteId = note.id) })
        }
        val eventsDetached = async {
            noteDao.detachEvents(eventsToRemove.map{ eventID -> EventNote(eventId = eventID, noteId = note.id) })
        }
        val weightsAttached = async {
            noteDao.attachWeights(weightsToAdd.map{ weight -> WeightNote(weightId=weight.id, noteId=note.id) })
        }
        val weightsDetached = async {
            noteDao.detachWeights(weightsToRemove.map{ weight -> WeightNote(weightId=weight.id, noteId=note.id) })
        }
        val photosAttached = photosToAdd.map { newPhoto ->
            async {
                mediaRepository.addNewPhotoForNote(newPhoto, note.id)
            }
        }
        val photosDetached = async {
            noteDao.detachPhotos(photosToRemove.map{ photo -> PhotoNote(photoId = photo.id, noteId=note.id) })
        }
        val tagRepository = TagRepository(database)
        val tagsAddedDeferred = tagsToAdd.map { tag ->
            async {
                attachTagToNote(tagRepository, note.id, tag)
            }
        }
        val tagsRemovedDeferred = tagsToRemove.map { tag ->
            async {
                tagRepository.detachNoteFromTag(note.id, tag)
            }
        }
        noteUpdated.await()
        petsAttached.await()
        petsDetached.await()
        eventsAttached.await()
        eventsDetached.await()
        weightsAttached.await()
        weightsDetached.await()
        photosDetached.await()
        photosAttached.awaitAll()
        tagsAddedDeferred.awaitAll()
        tagsRemovedDeferred.awaitAll()
    }

    private suspend fun attachTagToNote(tagRepository: TagRepository, noteId: Long, tag: Tag) = withContext(Dispatchers.IO) {
        if (tag.tagId == newTagPlaceholderId) {
            tagRepository.attachNoteToNewTag(noteId, tag)
        }
        else tagRepository.attachNoteToExistingTag(noteId, tag)
    }

    suspend fun insertPetNote(noteId: Long, petId: Long) {
        withContext(Dispatchers.IO) {
            noteDao.attachPet(PetNote(petId, noteId))
        }
    }

    suspend fun insertEventNote(noteId: Long, eventId: Long) {
        withContext(Dispatchers.IO) {
            noteDao.attachEvent(EventNote(eventId, noteId))
        }
    }

    suspend fun insertWeightNote(noteId: Long, weightId: Long) {
        withContext(Dispatchers.IO) {
            noteDao.attachWeight(WeightNote(weightId, noteId))
        }
    }

    suspend fun insertPhotoNote(noteId: Long, photoId: Long) = withContext(Dispatchers.IO){
        noteDao.attachPhoto(PhotoNote(photoId, noteId))
    }

    suspend fun getEventsOfNote(noteId: Long): List<Event> = withContext(Dispatchers.IO) {
        noteDao.getEventsOfNote(noteId)
    }

    suspend fun getPetsWithProfilePicsOfNote(noteId: Long): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        noteDao.getPetsWithProfilePicOfNote(noteId)
    }

    suspend fun getPhotosOfNote(noteId: Long): List<Photo> = withContext(Dispatchers.IO) {
        noteDao.getPhotosOfNoteAsList(noteId)
    }

    suspend fun getPhotosOfNotePaginated(noteId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo> = withContext(Dispatchers.IO) {
        noteDao.getPhotosOfNotePaginated(noteId, lastPhotoDate, lastPhotoId, amtLimit)
    }

    suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        noteDao.delete(note)
    }

    suspend fun getWeightsOfNote(noteId: Long): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        noteDao.getWeightsOfNote(noteId)
            .map{ WeightWithPetName(weight=it.weight, petName=it.assocPet.petName) }
            .sortedByDescending { it.weight.weightDateTime }
    }

    suspend fun getNoteEventsAsListPaginated(
        noteId: Long,
        lastEventDate: OffsetDateTime = OffsetDateTime.MAX,
        lastEventId: Long = Long.MAX_VALUE,
        eventAmt: Int
    ): List<Event> = withContext(Dispatchers.IO) {
        noteDao.getEventsOfNotePaginated(noteId, lastEventDate, lastEventId, eventAmt)
    }

    suspend fun getAllNotesPaginated(lastNoteUpdateDate: OffsetDateTime, lastNoteId: Long, noteAmt: Int): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getAllNotesPaginated(lastNoteUpdateDate, lastNoteId, noteAmt)
    }

    suspend fun getWeightsOfNotePaginated(noteId: Long, lastWeightDateTime: OffsetDateTime, lastWeightId: Long, weightsAmt: Int): List<WeightForListFetched> = withContext(Dispatchers.IO) {
        noteDao.getWeightsOfNotePaginated(noteId, lastWeightDateTime, lastWeightId, weightsAmt)
    }

    suspend fun getPetsOfNotePaginated(noteId: Long, lastPetId: Long, petsAmt: Int): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        noteDao.getPetsOfNotePaginated(noteId, lastPetId, petsAmt)
    }

}