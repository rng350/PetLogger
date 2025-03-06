package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.photos.data.Photo

class GetPhotosOfNoteUseCase(private val noteRepository: NoteRepository, private val noteId: Long):
    GetItemsUseCase<Photo> {
    override val onLastPage: Boolean = false
    override suspend fun invoke(): List<Photo> {
        return noteRepository.getPhotosOfNote(noteId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}