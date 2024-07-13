package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesForDisplayUseCase(private val noteRepository: NoteRepository): GetItemsForDisplayUseCase<Note> {
    override fun invoke(): Flow<List<Note>> {
        return noteRepository.getAllNotesAsFlow()
    }
}