package com.hfad.petlogger.repositories

import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Photo
import kotlinx.coroutines.Dispatchers
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
                       photos: List<Photo> = listOf<Photo>()) {
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
}