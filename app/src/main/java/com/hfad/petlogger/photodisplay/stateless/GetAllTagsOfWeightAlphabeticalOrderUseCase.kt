package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.WeightRepository

class GetAllTagsOfWeightAlphabeticalOrderUseCase(private val weightRepository: WeightRepository, private val weightId: Long): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return weightRepository.getTagsOfWeightAlphabeticalOrder(weightId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}