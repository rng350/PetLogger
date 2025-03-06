package com.hfad.petlogger.pets.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.pets.data.PetWithProfilePic

class GetPetsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long):
    GetItemsUseCase<PetWithProfilePic> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<PetWithProfilePic> {
        return noteRepository.getPetsWithProfilePicsOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}