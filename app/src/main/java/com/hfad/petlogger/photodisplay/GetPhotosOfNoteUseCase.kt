package com.hfad.petlogger.photodisplay

import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GetPhotosOfNoteUseCase(private val noteId: Long, private val noteRepository: NoteRepository): GetAssociatedPhotosUseCase {
    override fun invoke(): Flow<List<Photo>> {
        return noteRepository.getPhotosOfNote(noteId)
    }
}