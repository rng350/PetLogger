package com.hfad.petlogger.repositories

import com.hfad.petlogger.PetLoggerDatabase
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PetRepository(private val petDao: PetDao, private val mediaRepository: MediaRepository) {
    suspend fun getPet(petId: Long) = withContext(Dispatchers.IO) {
        petDao.get(petId)
    }

    suspend fun getAllPets(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        petDao.getAllPetsWithProfilePhotos()
    }

    suspend fun updatePet(pet: Pet) = withContext(Dispatchers.IO) {
        petDao.update(pet)
    }
}