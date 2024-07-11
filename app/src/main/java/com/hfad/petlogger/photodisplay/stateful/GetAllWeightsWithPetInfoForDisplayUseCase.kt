package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.repositories.WeightRepository
import kotlinx.coroutines.flow.Flow

class GetAllWeightsWithPetInfoForDisplayUseCase(private val weightRepository: WeightRepository): GetItemsForDisplayUseCase<WeightForList> {
    override fun invoke(): Flow<List<WeightForList>> {
        return weightRepository.getAllWeightsForDisplay()
    }
}