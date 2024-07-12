package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository

class GetAllNotesUseCase(private val noteRepository: NoteRepository): GetItemsUseCase<Note> {
    override suspend fun invoke(): List<Note> {
        return noteRepository.getAllNotes()
    }
}