package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.usecases.GetItemsUseCase

class GetAllNotesUseCase(private val noteRepository: NoteRepository): GetItemsUseCase<Note> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Note> {
        return noteRepository.getAllNotes()
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}