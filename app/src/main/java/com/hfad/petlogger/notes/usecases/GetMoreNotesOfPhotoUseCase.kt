package com.hfad.petlogger.notes.usecases

import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.MediaRepository
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.common.usecases.GetItemsUseCase

class GetMoreNotesOfPhotoUseCase(private val mediaRepository: MediaRepository,
                                 private val photoId: Long,
                                 private val notesAmt: Int
): GetItemsUseCase<Note> {
    private var lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        val notes = mediaRepository.getNotesOfPhotoPaginated(photoId, lastNoteEditedDate, lastNoteId, notesAmt)
        lastNoteId = notes.lastOrNull()?.id ?: Long.MAX_VALUE
        lastNoteEditedDate = notes.lastOrNull()?.lastUpdated ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = notes.size < notesAmt
        return notes
    }

    override fun resetCurrentPoint() {
        lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }
}