package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.weights.domain.WeightRepository

class GetNotesOfWeightUseCase(private val weightRepository: WeightRepository, private val weightId: Long):
    GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false

    override suspend fun invoke(): List<Note> {
        return weightRepository.getNotesOfWeight(weightId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}