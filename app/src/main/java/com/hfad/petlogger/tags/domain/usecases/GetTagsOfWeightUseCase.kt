package com.hfad.petlogger.tags.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.weights.domain.WeightRepository

class GetTagsOfWeightUseCase(private val weightRepository: WeightRepository, private val weightId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return weightRepository.getTagsOfWeight(weightId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}