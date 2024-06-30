package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.PetRepository

class GetPhotosOfPetUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsUseCase<Photo> {
    override suspend fun invoke(): List<Photo> {
        return petRepository.getPetPhotosAsList(petId)
    }
}