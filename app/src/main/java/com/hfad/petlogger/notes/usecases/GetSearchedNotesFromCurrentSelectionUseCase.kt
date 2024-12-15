package com.hfad.petlogger.notes.usecases

import androidx.lifecycle.LiveData
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class GetSearchedNotesFromCurrentSelectionUseCase(
    private val noteDao: NoteDao,
    private val currentSelection: LiveData<List<Note>>
): GetSearchedItemsUseCase<Note> {
    override var currentQuery: String = ""
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage
    private val queryBuilder = BuildNoteSearchQueryUseCase(get = BuildNoteSearchQueryUseCase.Get.OnlyIds)
    private var idsAlreadyFetched: Boolean = false
    private var fetchedIds: List<Long> = listOf()

    override suspend fun invoke(): List<Note> = withContext(Dispatchers.IO) {
        currentSelection.value?.let {  currentNoteSelection ->
            val currentSelectionMap = currentNoteSelection.associateBy { it.id }
            if (idsAlreadyFetched) {
                return@withContext fetchedIds.mapNotNull { currentSelectionMap[it] }
            }
            else {
                val builtQuery = queryBuilder(
                    query = currentQuery
                )
                builtQuery?.let { query ->
                    val searchResultsFetched = async {
                        noteDao.searchNoteIds(query)
                    }
                    val searchResults = searchResultsFetched.await()
                    fetchedIds = searchResults
                    idsAlreadyFetched = true
                    return@withContext searchResults.mapNotNull { currentSelectionMap[it] }
                }
            }
        }
        listOf()
    }

    // will only get called if query changes
    override fun resetCurrentPoint() {
        _onLastPage = false
        idsAlreadyFetched = false
        fetchedIds = listOf()
    }
}