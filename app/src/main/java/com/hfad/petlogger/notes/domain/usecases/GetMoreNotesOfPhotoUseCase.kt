package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.photos.domain.MediaRepository
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