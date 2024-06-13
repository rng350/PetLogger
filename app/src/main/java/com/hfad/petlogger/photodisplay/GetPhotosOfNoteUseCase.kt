package com.hfad.petlogger.photodisplay

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetPhotosOfNoteUseCase(private val noteId: Long, private val noteRepository: NoteRepository): GetAssociatedItemsUseCase<Photo> {
    override fun invoke(): Flow<List<Photo>> {
        return noteRepository.getPhotosOfNote(noteId)
    }
}