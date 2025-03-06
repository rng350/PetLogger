package com.hfad.petlogger.photos.domain.usecases

import com.hfad.petlogger.notes.domain.NoteRepository
import com.hfad.petlogger.photos.data.Photo
import java.time.OffsetDateTime

class GetMorePhotosOfNoteUseCase(
    private val noteRepository: NoteRepository,
    private val noteId: Long,
    private val photosAmt: Int
): GetPaginatedPhotosUseCase(photosAmt) {
    override suspend fun fetchPhotos(
        lastPhotoDate: OffsetDateTime,
        lastPhotoId: Long
    ): List<Photo> {
        return noteRepository.getPhotosOfNotePaginated(noteId, lastPhotoDate, lastPhotoId, photosAmt)
    }
}