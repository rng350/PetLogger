package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.NoteRepository

class GetPetsOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsUseCase<PetWithProfilePic> {
    override suspend fun invoke(): List<PetWithProfilePic> {
        return noteRepository.getPetsWithProfilePicsOfNote(noteId)
    }
}