package com.hfad.petlogger.pets.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.pets.PetWithProfilePic

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