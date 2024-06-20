package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.flow.Flow

class GetWeightsOfPetForDisplayUseCase(private val petRepository: PetRepository, private val petId: Long): GetAssociatedItemsForDisplayUseCase<Weight> {
    override fun invoke(): Flow<List<Weight>> {
        return petRepository.getPetWeights(petId)
    }
}