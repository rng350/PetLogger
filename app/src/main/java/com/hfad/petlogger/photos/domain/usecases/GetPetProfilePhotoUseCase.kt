package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.photos.data.Photo

class GetPetProfilePhotoUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetSingleItemUseCase<Photo> {
    override suspend fun invoke(): Photo? {
        return petRepository.getPetProfilePhoto(petId)
    }
}