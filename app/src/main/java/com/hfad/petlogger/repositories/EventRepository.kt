package com.hfad.petlogger.repositories

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventPet
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.PhotoEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EventRepository(private val database: PetLoggerDatabase,
                      private val mediaRepository: MediaRepository) {
    private val eventDao: EventDao = database.eventDao
    suspend fun get(eventId: Long): Event = withContext(Dispatchers.IO) {
        eventDao.get(eventId)
    }

    suspend fun getAll(): List<Event> = withContext(Dispatchers.IO) {
        eventDao.getAll()
    }

    suspend fun getPetsOfEvent(eventId: Long): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        eventDao.getPetsOfEventWithProfilePhotos(eventId)
    }

    suspend fun insert(event: Event,
                       pets: List<Pet> = listOf<Pet>(),
                       photos: List<Photo> = listOf<Photo>()) = withContext(Dispatchers.IO) {
        val eventId = async {
            eventDao.insert(event)
        }.await()
        val addEventPets = pets.map {pet ->
            async {
                addEventPet(EventPet(eventId, pet.petID))
            }
        }
        val addEventPhotos = photos.map {photo ->
            async {
                mediaRepository.addNewPhotoForEvent(photo, eventId)
            }
        }
        addEventPets.awaitAll()
        addEventPhotos.awaitAll()

        eventId
    }

    suspend fun update(event: Event,
                       petsToAdd: List<Pet> = listOf<Pet>(),
                       petsToRemove: List<Pet> = listOf<Pet>(),
                       photosToAdd: List<Photo> = listOf<Photo>(),
                       photosToRemove: List<Photo> = listOf<Photo>())
    = withContext(Dispatchers.IO) {
        val eventUpdated = async {
            eventDao.update(event)
        }
        val petDao = database.petDao
        val petsAdded = async {
            petDao.insert(petsToAdd.map{pet -> EventPet(eventId=event.eventId, petId=pet.petID)})
        }
        val petsDeleted = async {
            petDao.delete(petsToRemove.map{pet -> EventPet(eventId=event.eventId, petId=pet.petID)})
        }
        val photoDao = database.photoDao
        val photosAdded = photosToAdd.map { newPhoto ->
            async {
                mediaRepository.addNewPhotoForEvent(newPhoto, event.eventId)
            }
        }
        val photosDeleted =  async {
            photoDao.delete(photosToRemove.map{ photoToRemove -> PhotoEvent(photoID = photoToRemove.id, eventID = event.eventId) })
        }

        eventUpdated.await()
        petsAdded.await()
        petsDeleted.await()
        photosDeleted.await()
        photosAdded.awaitAll()
    }

    suspend fun delete(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.delete(event)
        }
    }

    suspend fun addEventPet(eventPet: EventPet) = withContext(Dispatchers.IO) {
        val eventPetDao = database.eventPetDao
        eventPetDao.insert(eventPet)
    }

    fun getPhotosOfEventAsFlow(eventId: Long): Flow<List<Photo>> {
        return eventDao.getPhotosOfEventAsFlow(eventId)
    }

    suspend fun getPhotosOfEvent(eventId: Long): List<Photo> = withContext(Dispatchers.IO) {
        eventDao.fetchPhotosOfEvent(eventId)
    }

    fun getPetsWithProfilePicOfEventAsFlow(eventId: Long): Flow<List<PetWithProfilePic>> {
        return eventDao.getPetsOfEventWithProfilePhotosAsFlow(eventId)
    }

    fun getAllEventsAsFlow(): Flow<List<Event>> {
        return eventDao.getAllEventsAsFlow()
    }
}