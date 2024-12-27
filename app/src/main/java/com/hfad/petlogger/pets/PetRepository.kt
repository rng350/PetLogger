package com.hfad.petlogger.pets

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.PetLoggerDatabase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventForList
import com.hfad.petlogger.common.associationentities.EventPet
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.common.associationentities.PetNote
import com.hfad.petlogger.common.associationentities.PetPhoto
import com.hfad.petlogger.common.associationentities.PetProfilePhoto
import com.hfad.petlogger.weights.PetWeightForSelection
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.common.util.Constants.Companion.newTagPlaceholderId
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.weights.PetWeightForDisplay
import com.hfad.petlogger.weights.PetWeightForDisplayFetched
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class PetRepository(private val database: PetLoggerDatabase, private val mediaRepository: MediaRepository) {
    private val petDao = database.petDao
    private val photoDao = database.photoDao
    private val noteDao = database.noteDao
    private val eventPetDao = database.eventPetDao
    suspend fun addPet(
        pet: Pet,
        events: List<Long> = listOf<Long>(),
        photos: List<Photo> = listOf<Photo>(),
        profilePic: Photo? = null,
        notes: List<Note> = listOf<Note>(),
        tags: List<Tag> = listOf<Tag>()
    ): Long = withContext(Dispatchers.IO) {
        Log.d("addPet", "PET BEING ADDED... Pet: ${pet.toString()}")
        val photosAdded = async {
            if (profilePic == null) {
                addPetPhotosNoProfilePic(pet, photos)
            }
            else {
                addPetPhotosWithProfilePic(pet, photos, profilePic)
            }
        }
        val petId = photosAdded.await()
        val eventsAdded = async {
            eventPetDao.insert(events.map{ eventId -> EventPet(eventId=eventId, petId=petId) })
        }
        val notesAdded = async {
            noteDao.attachPets(notes.map{ note -> PetNote(petId=petId, noteId=note.id) })
        }
        val tagRepository = TagRepository(database)
        val tagsAdded = tags.map { tag ->
            async {
                attachPetToTag(tagRepository, petId, tag)
            }
        }
        eventsAdded.await()
        notesAdded.await()
        tagsAdded.awaitAll()
        petId
    }

    suspend fun deletePet(pet: Pet) = withContext(Dispatchers.IO) {
        petDao.delete(pet)
    }

    suspend fun getPet(petId: Long) = withContext(Dispatchers.IO) {
        petDao.getPet(petId)
    }

    suspend fun getAllPets(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        petDao.getAllPetsWithProfilePhotos()
    }

    suspend fun getAllPetsPaginated(lastPetId: Long, petsAmt: Int): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        petDao.getAllPetsWithProfilePhotosPaginated(lastPetId, petsAmt)
    }

    suspend fun updatePet(
        pet: Pet,
        eventsToAdd: List<Long> = listOf<Long>(),
        eventsToRemove: List<Long> = listOf<Long>(),
        weightsToAdd: List<Weight> = listOf<Weight>(),
        weightsToRemove: List<Long> = listOf<Long>(),
        photosToAdd: List<Photo> = listOf<Photo>(),
        photosToRemove: List<Photo> = listOf<Photo>(),
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>(),
        tagsToAdd: List<Tag> = listOf<Tag>(),
        tagsToRemove: List<Tag> = listOf<Tag>(),
        petProfilePhotoToAdd: Photo? = null,
        petProfilePhotoToRemove: Photo? = null
    ) = withContext(Dispatchers.IO) {
        val weightDao = database.weightDao
        val petUpdated = async {
            petDao.update(pet)
        }
        val petEventsInserted = async {
            petDao.insert(eventsToAdd.map { eventId -> EventPet(eventId=eventId, petId=pet.petID) })
        }
        val petEventsDeleted = async {
            petDao.delete(eventsToRemove.map { eventId -> EventPet(eventId=eventId, petId=pet.petID) })
        }
        val petWeightsInserted = async {
            weightDao.insert(weightsToAdd)
        }
        val petWeightsDeleted = async {
            weightDao.deleteWeightsById(weightsToRemove)
        }
        val petPhotosAdded = async {
            addPetPhotos(petId=pet.petID, photos=photosToAdd, profilePic = petProfilePhotoToAdd)
        }
        val petPhotosDeleted = async {
            removePetPhotos(petId=pet.petID, photosToRemove=photosToRemove)
        }
        val petProfilePicDeleted = async {
            petProfilePhotoToRemove?.let { profilePhoto ->
                photoDao.delete(PetProfilePhoto(petID = pet.petID, photoID = profilePhoto.id))
            }
        }
        val notesAdded = async {
            noteDao.attachPets(notesToAdd.map{ note -> PetNote(petId=pet.petID, noteId = note.id) })
        }
        val notesRemoved = async {
            noteDao.detachPets(notesToRemove.map{ note -> PetNote(petId=pet.petID, noteId=note.id) })
        }
        val notesUpdated = notesToUpdate.map { note ->
            async {
                noteDao.update(note)
            }
        }
        val tagRepository = TagRepository(database)
        val tagsAdded = tagsToAdd.map { tag ->
            async {
                attachPetToTag(tagRepository, pet.petID, tag)
            }
        }
        val tagsRemoved = tagsToRemove.map { tag ->
            async {
                tagRepository.detachPetFromTag(petId = pet.petID, tag)
            }
        }
        petUpdated.await()
        petEventsInserted.await()
        petEventsDeleted.await()
        petWeightsInserted.await()
        petWeightsDeleted.await()
        petPhotosAdded.await()
        petPhotosDeleted.await()
        petProfilePicDeleted.await()
        notesAdded.await()
        notesRemoved.await()
        notesUpdated.awaitAll()
        tagsAdded.awaitAll()
        tagsRemoved.awaitAll()
    }

    suspend fun getPetProfilePhoto(petId: Long): Photo? = withContext(Dispatchers.IO) {
        petDao.getPetProfilePhoto(petId)
    }

    suspend fun getPetPhotosAsList(petId: Long): List<Photo> = withContext(Dispatchers.IO) {
        petDao.getPetPhotosAsList(petId)
    }

    suspend fun getPetWeightsPaginated(
        petId: Long,
        lastWeightDate: OffsetDateTime,
        lastWeightId: Long,
        amtLimit: Int
    ): List<PetWeightForDisplayFetched> = withContext(Dispatchers.IO) {
        petDao.getWeightsOfPetPaginated(petId, lastWeightDate, lastWeightId, amtLimit)
    }

    suspend fun getPetEventsAsList(petId: Long): List<Event> = withContext(Dispatchers.IO) {
        petDao.getEventsOfPet(petId)
    }

    suspend fun getPetEventsAsListPaginated(
        petId: Long,
        lastEventDate: OffsetDateTime,
        lastEventId: Long,
        eventAmt: Int
    ): List<Event> = withContext(Dispatchers.IO) {
        Log.d("PetRepository", "getPetEventsPaginated, LastEventDate: ${lastEventDate}")
        petDao.getEventsOfPetPaginated(petId, lastEventDate, lastEventId, eventAmt)
        //petDao.getEventsOfPetPaginatedCC(petId, lastEventDate, eventAmt)
    }

    suspend fun addPetPhotos(petId: Long, photos: List<Photo> = listOf<Photo>(), profilePic: Photo? = null) = withContext(Dispatchers.IO) {
        var photosToAdd = photos
        val newPhotosList = photos.toMutableList()
        profilePic?.let {
            newPhotosList.remove(profilePic) // prevent profilePic from appearing twice in photo list
            photosToAdd = listOf(profilePic) + newPhotosList
        }
        photosToAdd.map { photo ->
            async {
                if (photo == profilePic) {
                    addPetProfilePhoto(petId, photo)
                }
                else {
                    addPetPhoto(petId, photo)
                }
            }
        }.awaitAll()
    }

    suspend fun removePetPhotos(petId: Long, photosToRemove: List<Photo> = listOf<Photo>()) = withContext(Dispatchers.IO) {
        petDao.deletePetPhotos(photosToRemove.map{photo -> PetPhoto(petId=petId, photoId=photo.id) })
    }

    private suspend fun addPetPhotosNoProfilePic(pet: Pet, photos: List<Photo> = listOf<Photo>()): Long = withContext(Dispatchers.IO){
        val petInserted = async {
            petDao.insertPet(pet)
        }.await()
        photos.map { photo ->
            async {
                addPetPhoto(petID = petInserted.petID, photo = photo)
            }
        }.awaitAll()
        petInserted.petID
    }

    private suspend fun addPetPhotosWithProfilePic(pet: Pet, photos: List<Photo> = listOf<Photo>(), profilePic: Photo): Long = withContext(Dispatchers.IO){
        val petInsertedDeferred = async {
            petDao.insertPet(pet)
        }
        val petInserted = petInsertedDeferred.await()
        val newPhotosList = photos.toMutableList()
        newPhotosList.remove(profilePic) // prevent profilePic from appearing twice in photo list
        val combinedPhotos: List<Photo> = listOf(profilePic) + newPhotosList
        combinedPhotos.map { photo ->
            async {
                if (photo == profilePic) {
                    addPetProfilePhoto(petInserted.petID, photo)
                }
                else {
                    addPetPhoto(petInserted.petID, photo)
                }
            }
        }.awaitAll()
        petInserted.petID
    }

    private suspend fun addPetPhoto(petID: Long, photo: Photo) {
        withContext(Dispatchers.IO) {
            val photoAdded = async {
                mediaRepository.addPhoto(photo)
            }.await()
            photoAdded?.let {
                Log.d("PetRep:addPetPhoto", "Photo about to be linked... ${it}")
                petDao.insertPetPhoto(PetPhoto(photoId = photo.id, petId = petID))
                Log.d("PetRep:addPetProfPhoto", "Photo linked to Pet... ${it}")
            }
        }
    }

    private suspend fun addPetProfilePhoto(petID: Long, photo: Photo) {
        withContext(Dispatchers.IO) {
            val photoAdded = async {
                mediaRepository.addPhoto(photo)
            }.await()
            photoAdded?.let {
                Log.d("PetRep:addPetProfPhoto", "Photo about to be linked... ${it}")
                petDao.insertPetPhoto(PetPhoto(photoId = photo.id, petId = petID))
                Log.d("PetRep:addPetProfPhoto", "Photo linked to Pet... ${it}")
                photoDao.insert(PetProfilePhoto(photoID = photo.id, petID = petID))
                Log.d("PetRep:addPetProfPhoto", "Photo set as PetProfilePhoto... ${it}")
            }
        }
    }

    suspend fun getTagsOfPetAlphabeticalOrder(petId: Long) = withContext(Dispatchers.IO) {
        petDao.getAllTagsOfPetAlphabeticalOrder(petId)
    }

    suspend fun getPetNotesPaginated(
        petId: Long,
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long,
        notesAmt: Int)
    : List<Note> = withContext(Dispatchers.IO) {
        petDao.getNotesOfPetPaginated(petId, lastNoteUpdateDate, lastNoteId, notesAmt)
    }

    suspend fun getPhotosOfPetPaginated(petId: Long, lastPhotoDate: OffsetDateTime, lastPhotoId: Long, amtLimit: Int): List<Photo> = withContext(Dispatchers.IO) {
        petDao.getPhotosOfPetPaginated(petId, lastPhotoDate, lastPhotoId, amtLimit)
    }

    suspend fun getNotesOfPet(petId: Long): List<Note> = withContext(Dispatchers.IO) {
        petDao.getNotesOfPet(petId)
    }

    private suspend fun attachPetToTag(tagRepository: TagRepository, petId: Long, tag: Tag) {
        if (tag.tagId == newTagPlaceholderId) {
            tagRepository.attachPetToNewTag(petId, tag)
        } else {
            tagRepository.attachPetToExistingTag(petId, tag)
        }
    }

    suspend fun getTagsOfPet(petId: Long): List<Tag> = withContext(Dispatchers.IO) {
        petDao.getAllTagsOfPet(petId)
    }
}