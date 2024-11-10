package com.hfad.petlogger.screens.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.notes.usecases.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.notes.usecases.GetMoreOfSearchedNotesFromAllUseCase
import com.hfad.petlogger.notes.NoteRepository
import com.hfad.petlogger.common.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val getInitialNoteList: GetItemsUseCase<Note>,
    private val getSearchedNotes: GetSearchedItemsUseCase<Note>
) : ViewModel() {
    private var currentNoteGetter: GetItemsUseCase<Note> = getInitialNoteList
    private val _notes: MutableStateFlow<List<Note>> = MutableStateFlow(listOf())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    val noteNavigator = Navigator()
    private var isLoading: Boolean = false
    init {
        reload()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedNotes = currentNoteGetter()
            _notes.update { it + loadedNotes }
            isLoading = false
        }
    }

    private fun reload() {
        viewModelScope.launch {
            isLoading = true
            val loadedNotes = currentNoteGetter()
            _notes.update { loadedNotes }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return currentNoteGetter.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    fun onQueryTextSubmit(query: String?) {
        if (query != null) {
            reinitializeGetterType(query)
        } else {
            Log.d("NoteListVM", "onQueryTextSubmit: Query is null")
        }
    }

    fun onQueryTextChanged(newText: String?) {
        if (newText != null) {
            reinitializeGetterType(newText)
        } else {
            Log.d("NoteListVM", "onQueryTextChanged: Query is null")
        }
    }

    private fun reinitializeGetterType(query: String) {
        if (query.isNotEmpty()) {
            getSearchedNotes.changeSearchQuery("${query}*")
            currentNoteGetter = getSearchedNotes
        } else {
            currentNoteGetter = getInitialNoteList
            currentNoteGetter.resetCurrentPoint()
        }
        reload()
    }

    companion object {
        fun provideFactory(getInitialNoteList: GetItemsUseCase<Note>, getSearchedNotes: GetSearchedItemsUseCase<Note>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
                    return NoteListViewModel(getInitialNoteList, getSearchedNotes) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}