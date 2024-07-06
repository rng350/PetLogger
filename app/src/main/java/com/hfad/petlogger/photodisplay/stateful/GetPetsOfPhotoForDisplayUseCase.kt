package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetPetsOfPhotoForDisplayUseCase(private val mediaRepository: MediaRepository, private val photoId: Long): GetItemsForDisplayUseCase<PetWithProfilePic> {
    override fun invoke(): Flow<List<PetWithProfilePic>> {
        return mediaRepository.getPetsOfPhoto(photoId)
    }
}