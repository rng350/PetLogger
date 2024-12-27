package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo
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