package com.hfad.petlogger.photos.usecases

import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo

class GetMorePhotosOfNoteUseCase(private val noteRepository: NoteRepository,
                                 private val noteId: Long,
                                 private val photosAmt: Int
): GetItemsUseCase<Photo> {
    private var lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastPhotoId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Photo> {
        val photos = noteRepository.getPhotosOfNotePaginated(noteId, lastPhotoDate, lastPhotoId, photosAmt)
        lastPhotoId = photos.lastOrNull()?.id ?: Long.MAX_VALUE
        lastPhotoDate = photos.lastOrNull()?.date ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = photos.size < photosAmt
        return photos
    }

    override fun resetCurrentPoint() {
        lastPhotoDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastPhotoId = Long.MAX_VALUE
        _onLastPage = false
    }
}