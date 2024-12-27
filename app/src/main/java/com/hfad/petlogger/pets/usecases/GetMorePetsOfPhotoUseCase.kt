package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.MediaRepository

class GetMorePetsOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return mediaRepository.getPetsOfPhotoPaginated(photoId, lastPetId, petsAmt)
    }

}