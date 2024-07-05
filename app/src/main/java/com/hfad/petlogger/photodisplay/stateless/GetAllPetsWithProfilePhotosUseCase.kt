package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllPetsWithProfilePhotosUseCase(private val petRepository: PetRepository): GetItemsUseCase<PetWithProfilePic> {
    override suspend fun invoke(): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        petRepository.getAllPets()
    }
}