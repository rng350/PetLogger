package com.hfad.petlogger.repositories

import androidx.room.withTransaction
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventNote
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetNote
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoNote
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightNote
import com.hfad.petlogger.entities.WeightWithPetName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(
    private val database: PetLoggerDatabase,
    private val mediaRepository: MediaRepository
) {
    private val noteDao = database.noteDao
    suspend fun getNote(noteId: Long): Note
    = withContext(Dispatchers.IO) {
        noteDao.get(noteId)
    }

    suspend fun getPetsOfNote(noteId: Long): List<Pet>
    = withContext(Dispatchers.IO) {
        noteDao.getPetsOfNote(noteId)
    }

    suspend fun getAllNotes(): List<Note> {
        return noteDao.getAll()
    }

    suspend fun insertNote(note: Note,
                           pets: List<Pet> = listOf<Pet>(),
                           events: List<Event> = listOf<Event>(),
                           weights: List<Weight> = listOf<Weight>(),
                           photos: List<Photo> = listOf<Photo>()): Long
    = withContext(Dispatchers.IO) {

        val noteId = database.withTransaction {
            noteDao.insert(note)
        }

        val petsDeferred = pets.map {
            async {
                insertPetNote(noteId, it.petID)
            }
        }
        val eventsDeferred = events.map {
            async {
                insertEventNote(noteId, it.eventId)
            }
        }
        val weightsDeferred = weights.map {
            async {
                insertWeightNote(noteId, it.id)
            }
        }
        val photosDeferred = photos.map {
            async {
                mediaRepository.addNewPhotoForNote(it, noteId)
            }
        }

        petsDeferred.awaitAll()
        eventsDeferred.awaitAll()
        weightsDeferred.awaitAll()
        photosDeferred.awaitAll()

        noteId
    }

    suspend fun updateNote(note: Note,
                           petsToAdd: List<Pet> = listOf<Pet>(),
                           petsToRemove: List<Pet> = listOf<Pet>(),
                           eventsToAdd: List<Event> = listOf<Event>(),
                           eventsToRemove: List<Event> = listOf<Event>(),
                           weightsToAdd: List<Weight> = listOf<Weight>(),
                           weightsToRemove: List<Weight> = listOf<Weight>(),
                           photosToAdd: List<Photo> = listOf<Photo>(),
                           photosToRemove: List<Photo> = listOf<Photo>())
    = withContext(Dispatchers.IO) {
        val noteUpdated = async {
            noteDao.update(note)
        }
        val petsAttached = async {
            noteDao.attachPets(petsToAdd.map{ pet -> PetNote(petId=pet.petID, noteId=note.id)})
        }
        val petsDetached = async {
            noteDao.detachPets(petsToRemove.map{ pet -> PetNote(petId=pet.petID, noteId=note.id)})
        }
        val eventsAttached = async {
            noteDao.attachEvents(eventsToAdd.map{ event -> EventNote(eventtId = event.eventId, noteId = note.id)})
        }
        val eventsDetached = async {
            noteDao.detachEvents(eventsToRemove.map{ event -> EventNote(eventtId = event.eventId, noteId = note.id)})
        }
        val weightsAttached = async {
            noteDao.attachWeights(weightsToAdd.map{ weight -> WeightNote(weightId=weight.id, noteId=note.id)})
        }
        val weightsDetached = async {
            noteDao.detachWeights(weightsToRemove.map{ weight -> WeightNote(weightId=weight.id, noteId=note.id)})
        }
        val photosAttached = photosToAdd.map { newPhoto ->
            async {
                mediaRepository.addNewPhotoForNote(newPhoto, note.id)
            }
        }
        val photosDetached = async {
            noteDao.detachPhotos(photosToRemove.map{ photo -> PhotoNote(photoId = photo.id, noteId=note.id)})
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

    fun getPhotosOfNoteAsFlow(noteId: Long): Flow<List<Photo>> {
        return noteDao.getPhotosOfNote(noteId)
    }

    fun getPetsWithProfilePicsOfNoteAsFlow(noteId: Long): Flow<List<PetWithProfilePic>> {
        return noteDao.getPetsWithProfilePicOfNoteAsFlow(noteId)
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

    suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        noteDao.delete(note)
    }

    suspend fun getWeightsOfNote(noteId: Long): List<WeightWithPetName> = withContext(Dispatchers.IO) {
        noteDao.getWeightsOfNote(noteId)
            .map{WeightWithPetName(weight=it.weight, petName=it.assocPet.petName)}
            .sortedByDescending { it.weight.weightDateTime }
    }

    fun getEventsOfNoteAsFlow(noteId: Long): Flow<List<Event>> {
        return noteDao.getEventsOfNoteAsFlow(noteId)
    }
}