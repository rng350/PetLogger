package com.hfad.petlogger.notes.domain.usecases

import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Constants
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.notes.data.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class GetMoreOfSearchedNotesUseCase(
    private val noteDao: NoteDao,
    private val notesAmt: Int,
    pickFrom: BuildNoteSearchQueryUseCase.Pick? = null
    ): GetSearchedItemsUseCase<Note> {
    override var currentQuery: String = ""
    private var lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
    private var lastNoteId = Long.MAX_VALUE
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    val queryBuilder = BuildNoteSearchQueryUseCase(notesAmt = notesAmt, pickFrom =  pickFrom)

    override suspend fun invoke(): List<Note> = withContext(Dispatchers.IO) {
        val queryBuilt = queryBuilder(currentQuery, lastNoteEditedDate, lastNoteId)
        queryBuilt?.let { query ->
            val searchResults = noteDao.searchNotes(query)
            lastNoteEditedDate = searchResults.lastOrNull()?.lastUpdated ?: OffsetDateTime.MIN
            lastNoteId = searchResults.lastOrNull()?.id ?: Long.MIN_VALUE
            _onLastPage = searchResults.size < notesAmt
            return@withContext searchResults
        }
        listOf()
    }

    override fun resetCurrentPoint() {
        lastNoteEditedDate = Constants.OFFSET_DATE_TIME_MAX_ALLOWED
        lastNoteId = Long.MAX_VALUE
        _onLastPage = false
    }
}