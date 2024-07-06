package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.NoteRepository

class GetPhotosOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsUseCase<Photo> {
    override suspend fun invoke(): List<Photo> {
        return noteRepository.getPhotosOfNote(noteId)
    }
}