package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.WeightRepository

class GetNotesOfWeightUseCase(private val weightRepository: WeightRepository, private val weightId: Long): GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return weightRepository.getNotesOfWeight(weightId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}