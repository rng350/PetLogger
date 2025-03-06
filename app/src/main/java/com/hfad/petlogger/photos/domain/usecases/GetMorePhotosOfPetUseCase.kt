package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.photos.data.Photo
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