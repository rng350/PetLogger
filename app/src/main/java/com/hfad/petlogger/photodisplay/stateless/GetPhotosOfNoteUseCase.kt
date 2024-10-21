package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.NoteRepository

class GetPhotosOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long): GetItemsUseCase<Photo> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Photo> {
        return noteRepository.getPhotosOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}