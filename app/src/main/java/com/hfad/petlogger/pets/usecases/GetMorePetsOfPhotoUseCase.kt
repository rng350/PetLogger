package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.MediaRepository

class GetMorePetsOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val petsAmt: Int
): GetItemsUseCase<PetWithProfilePic> {
    private var lastPetId = Long.MIN_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<PetWithProfilePic> {
        val pets = mediaRepository.getPetsOfPhotoPaginated(photoId, lastPetId, petsAmt)
        lastPetId = pets.lastOrNull()?.petId ?: Long.MAX_VALUE
        _onLastPage = pets.size < petsAmt
        return pets
    }

    override fun resetCurrentPoint() {
        lastPetId = Long.MIN_VALUE
        _onLastPage = false
    }
}