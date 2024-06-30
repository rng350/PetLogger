package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.PetRepository

class GetPetProfilePhotoUseCase(private val petRepository: PetRepository, private val petId: Long): GetSingleItemUseCase<Photo> {
    override suspend fun invoke(): Photo? {
        return petRepository.getPetProfilePhoto(petId)
    }
}