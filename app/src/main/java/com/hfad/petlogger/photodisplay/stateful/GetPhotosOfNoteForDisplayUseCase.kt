package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetPhotosOfNoteForDisplayUseCase(private val noteId: Long, private val noteRepository: NoteRepository):
    GetItemsForDisplayUseCase<Photo> {
    override fun invoke(): Flow<List<Photo>> {
        return noteRepository.getPhotosOfNoteAsFlow(noteId)
    }
}