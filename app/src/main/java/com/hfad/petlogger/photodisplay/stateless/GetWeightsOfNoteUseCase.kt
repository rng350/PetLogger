package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.repositories.NoteRepository

class GetWeightsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsUseCase<WeightWithPetName> {
    override suspend fun invoke(): List<WeightWithPetName> {
        return noteRepository.getWeightsOfNote(noteId)
    }
}