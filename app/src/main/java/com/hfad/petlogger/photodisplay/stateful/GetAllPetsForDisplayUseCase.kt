package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.flow.Flow

class GetAllPetsForDisplayUseCase(private val petRepository: PetRepository): GetItemsForDisplayUseCase<PetWithProfilePic> {
    override fun invoke(): Flow<List<PetWithProfilePic>> {
        return petRepository.getAllPetsAsFlow()
    }
}