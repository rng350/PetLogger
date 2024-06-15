package com.hfad.petlogger.repositories

import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventPet
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EventRepository(private val database: PetLoggerDatabase,
                      private val mediaRepository: MediaRepository) {
    private val eventDao: EventDao = database.eventDao
    suspend fun get(eventId: Long) {

    }

    suspend fun getAll(): List<Event> = withContext(Dispatchers.IO) {
        eventDao.getAll()
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
                       photosToRemove: List<Photo> = listOf<Photo>()) {
    }

    suspend fun delete(event: Event,
                       pets: List<Pet> = listOf<Pet>(),
                       photos: List<Photo> = listOf<Photo>()) {

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
}