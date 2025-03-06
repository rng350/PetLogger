package com.hfad.petlogger.weights.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.weights.data.WeightWithPetName

class GetWeightsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long):
    GetItemsUseCase<WeightWithPetName> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<WeightWithPetName> {
        return noteRepository.getWeightsOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}