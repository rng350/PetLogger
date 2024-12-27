package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import java.time.OffsetDateTime

class GetMoreNotesOfPhotoUseCase(
    private val mediaRepository: MediaRepository,
    private val photoId: Long,
    private val notesAmt: Int
): GetPaginatedNotesUseCase(notesAmt) {
    override suspend fun fetchNotes(
        lastNoteUpdateDate: OffsetDateTime,
        lastNoteId: Long
    ): List<Note> {
        return mediaRepository.getNotesOfPhotoPaginated(photoId, lastNoteUpdateDate, lastNoteId, notesAmt)
    }
}