package com.hfad.petlogger.repositories

import android.util.Log
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.copyOf
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventPet
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetPhoto
import com.hfad.petlogger.entities.PetProfilePhoto
import com.hfad.petlogger.entities.PetWeightForDisplay
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PetRepository(private val database: PetLoggerDatabase, private val mediaRepository: MediaRepository) {
    private val petDao = database.petDao
    private val photoDao = database.photoDao
    suspend fun addPet(pet: Pet, photos: List<Photo> = listOf<Photo>(), profilePic: Photo? = null) = withContext(Dispatchers.IO) {
        if (profilePic == null) {
            addPetPhotosNoProfilePic(pet, photos)
        }
        else {
            addPetPhotosWithProfilePic(pet, photos, profilePic)
        }
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

    suspend fun updatePet(
        pet: Pet,
        eventsToAdd: List<Event> = listOf<Event>(),
        eventsToRemove: List<Event> = listOf<Event>(),
        weightsToAdd: List<Weight> = listOf<Weight>(),
        weightsToRemove: List<Weight> = listOf<Weight>(),
        photosToAdd: List<Photo> = listOf<Photo>(),
        photosToRemove: List<Photo> = listOf<Photo>(),
        petProfilePhotoToAdd: Photo? = null,
        petProfilePhotoToRemove: Photo? = null
    ) = withContext(Dispatchers.IO) {
        val weightDao = database.weightDao
        val petUpdated = async {
            petDao.update(pet)
        }
        val petEventsInserted = async {
            petDao.insert(eventsToAdd.map { EventPet(eventId=it.eventId, petId=pet.petID) })
        }
        val petEventsDeleted = async {
            petDao.delete(eventsToRemove.map { EventPet(eventId=it.eventId, petId=pet.petID) })
        }
        val petWeightsInserted = async {
            weightDao.insert(weightsToAdd)
        }
        val petWeightsDeleted = async {
            weightDao.delete(weightsToRemove)
        }
        val petPhotosAdded = async {
            addPetPhotos(petId=pet.petID, photos=photosToAdd, profilePic = petProfilePhotoToAdd)
        }
        val petPhotosDeleted = async {
            removePetPhotos(petId=pet.petID, photosToRemove=photosToRemove)
        }
        petProfilePhotoToRemove?.let { profilePhoto ->
            photoDao.delete(PetProfilePhoto(petID = pet.petID, photoID = profilePhoto.id))
        }
        petUpdated.await()
        petEventsInserted.await()
        petEventsDeleted.await()
        petWeightsInserted.await()
        petWeightsDeleted.await()
        petPhotosAdded.await()
        petPhotosDeleted.await()
    }

    suspend fun setPetProfilePhoto(petId: Long, photoId: Long) = withContext(Dispatchers.IO) {
        photoDao.insert(PetProfilePhoto(petID = petId, photoID = photoId))
    }

    suspend fun getPetProfilePhoto(petId: Long): Photo? = withContext(Dispatchers.IO) {
        petDao.getPetProfilePhoto(petId)
    }

    fun getPetPhotos(petId: Long): Flow<List<Photo>> {
        return petDao.getPetPhotos(petId)
    }

    suspend fun getPetPhotosAsList(petId: Long): List<Photo> = withContext(Dispatchers.IO) {
        petDao.getPetPhotosAsList(petId)
    }

    fun getPetEvents(petId: Long): Flow<List<Event>> {
        return petDao.getPetEvents(petId)
    }

    fun getPetWeights(petId: Long): Flow<List<Weight>> {
        return petDao.getPetWeights(petId)
    }

    suspend fun getPetWeightsAsList(petId: Long): List<Weight> = withContext(Dispatchers.IO) {
        petDao.getWeightsOfPet(petId).toList()
    }

    suspend fun getCheckablePetEventsAsList(petId: Long): List<CheckableItem<Event>> = withContext(Dispatchers.IO) {
        petDao.getEventsOfPet(petId).map {event ->
            CheckableItem(event)
        }
    }

    suspend fun getPetEventsAsList(petId: Long): List<Event> = withContext(Dispatchers.IO) {
        petDao.getEventsOfPet(petId)
    }

    suspend fun getCheckablePetWeightsWithTextFields(petId: Long): List<CheckableItem<PetWeightForDisplay>> = withContext(Dispatchers.IO) {
        val getDateDisplay = GetDateDisplayUseCase()
        val getTimeDisplay = GetTimeDisplayUseCase()
        petDao.getWeightsOfPet(petId).map { weight ->
            CheckableItem(
                PetWeightForDisplay(
                    weight,
                    getDateDisplay(weight.weightDateTime),
                    getTimeDisplay(weight.weightDateTime),
                    "${weight.weightGrams}g"
                )
            )
        }
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
        petDao.deletePetPhotos(photosToRemove.map{photo -> PetPhoto(petId=petId, photoId=photo.id)})
    }

    private suspend fun addPetPhotosNoProfilePic(pet: Pet, photos: List<Photo> = listOf<Photo>()): Long = withContext(Dispatchers.IO){
        val petInserted = async {
            val rowId = petDao.insert(pet)
            petDao.getPetFromRow(rowId)
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
            val rowId = petDao.insert(pet)
            petDao.getPetFromRow(rowId)
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
}