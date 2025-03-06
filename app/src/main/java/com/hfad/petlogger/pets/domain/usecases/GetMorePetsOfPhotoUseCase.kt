package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.photos.domain.MediaRepository

class GetMorePetsOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val petsAmt: Int
): GetPaginatedPetsUseCase(petsAmt) {
    override suspend fun fetchPets(lastPetId: Long): List<PetWithProfilePic> {
        return mediaRepository.getPetsOfPhotoPaginated(photoId, lastPetId, petsAmt)
    }

}