package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo
import java.time.OffsetDateTime

class GetMorePhotosOfPetUseCase(
    private val petRepository: PetRepository,
    private val petId: Long,
    private val photosAmt: Int
): GetPaginatedPhotosUseCase(photosAmt) {
    override suspend fun fetchPhotos(
        lastPhotoDate: OffsetDateTime,
        lastPhotoId: Long
    ): List<Photo> {
        return petRepository.getPhotosOfPetPaginated(petId, lastPhotoDate, lastPhotoId, photosAmt)
    }
}