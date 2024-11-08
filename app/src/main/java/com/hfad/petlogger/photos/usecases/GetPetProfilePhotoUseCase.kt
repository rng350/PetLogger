package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.photos.Photo

class GetPetProfilePhotoUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetSingleItemUseCase<Photo> {
    override suspend fun invoke(): Photo? {
        return petRepository.getPetProfilePhoto(petId)
    }
}