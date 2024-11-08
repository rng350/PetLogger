package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.WeightRepository

class GetAllTagsOfWeightAlphabeticalOrderUseCase(private val weightRepository: WeightRepository, private val weightId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return weightRepository.getTagsOfWeightAlphabeticalOrder(weightId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}