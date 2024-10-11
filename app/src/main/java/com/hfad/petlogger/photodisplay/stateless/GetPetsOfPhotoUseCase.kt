package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.MediaRepository

class GetPetsOfPhotoUseCase(private val mediaRepository: MediaRepository, private val photoId: Long): GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<PetWithProfilePic> {
        return mediaRepository.getPetsOfPhoto(photoId)
    }
}