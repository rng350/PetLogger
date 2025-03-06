package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.photos.domain.MediaRepository

class GetPetsOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long):
    GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<PetWithProfilePic> {
        return mediaRepository.getPetsOfPhoto(photoId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}