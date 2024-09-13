package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository

class GetAllNotesUseCase(private val noteRepository: NoteRepository): GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Note> {
        return noteRepository.getAllNotes()
    }
}