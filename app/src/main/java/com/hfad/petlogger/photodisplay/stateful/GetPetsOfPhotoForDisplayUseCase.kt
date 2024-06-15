package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetPetsOfPhotoForDisplayUseCase(private val photoId: Long, private val mediaRepository: MediaRepository): GetAssociatedItemsForDisplayUseCase<PetWithProfilePic> {
    override fun invoke(): Flow<List<PetWithProfilePic>> {
        TODO("Not yet implemented")
    }
}