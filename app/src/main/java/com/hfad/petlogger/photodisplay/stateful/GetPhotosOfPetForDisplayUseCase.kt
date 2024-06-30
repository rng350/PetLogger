package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.flow.Flow

class GetPhotosOfPetForDisplayUseCase(private val petRepository: PetRepository, private val petId: Long): GetItemsForDisplayUseCase<Photo> {
    override fun invoke(): Flow<List<Photo>> {
        return petRepository.getPetPhotos(petId)
    }
}