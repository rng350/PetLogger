package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.util.Constants

class GetMoreNotesOfWeightUseCase(private val weightRepository: WeightRepository,
                                  private val weightId: Long,
                                  private val notesAmt: Int
): GetItemsUseCase<Note> {
    private var lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Note> {
        val notes = weightRepository.getNotesOfWeightPaginated(weightId, lastNoteEditedDate, lastNoteId, notesAmt)
        lastNoteId = notes.lastOrNull()?.id ?: Long.MAX_VALUE
        lastNoteEditedDate = notes.lastOrNull()?.lastUpdated ?: Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        _onLastPage = notes.size < notesAmt
        return notes
    }
}