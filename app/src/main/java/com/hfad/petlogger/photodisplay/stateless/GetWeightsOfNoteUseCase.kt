package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.repositories.NoteRepository

class GetWeightsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsUseCase<WeightWithPetName> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<WeightWithPetName> {
        return noteRepository.getWeightsOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}