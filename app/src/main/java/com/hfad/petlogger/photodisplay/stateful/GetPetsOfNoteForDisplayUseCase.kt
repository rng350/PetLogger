package com.hfad.petlogger.photodisplay.stateful

import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetPetsOfNoteForDisplayUseCase(private val noteId: Long, private val noteRepository: NoteRepository): GetAssociatedItemsForDisplayUseCase<PetWithProfilePic> {
    override fun invoke(): Flow<List<PetWithProfilePic>> {
        return noteRepository.getPetsWithProfilePicsOfNoteAsFlow(noteId)
    }
}