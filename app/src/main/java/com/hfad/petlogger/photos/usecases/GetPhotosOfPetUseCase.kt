package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo

class GetPhotosOfPetUseCase(private val petRepository: PetRepository, private val petId: Long):
    GetItemsUseCase<Photo> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Photo> {
        return petRepository.getPetPhotosAsList(petId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}