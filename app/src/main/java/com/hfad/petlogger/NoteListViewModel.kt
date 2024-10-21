package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreOfAllNotesUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreOfSearchedNotesFromAllUseCase
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val noteRepository: NoteRepository,
    private val notesAmt: Int
) : ViewModel() {
    private val getAllNotes = GetMoreOfAllNotesUseCase(noteRepository, notesAmt)
    private var currentNoteGetter: GetItemsUseCase<Note> = getAllNotes
    private val _notes: MutableStateFlow<List<Note>> = MutableStateFlow(listOf())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    val noteNavigator = Navigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedNotes = currentNoteGetter()
            _notes.update { it + loadedNotes }
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
            currentNoteGetter = GetMoreOfSearchedNotesFromAllUseCase(noteRepository, notesAmt, "${query}*")
        } else {
            currentNoteGetter = getAllNotes
            currentNoteGetter.resetCurrentPoint()
        }
        _notes.update { listOf() }
        load()
    }

    companion object {
        fun provideFactory(noteRepository: NoteRepository, notesAmt: Int): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
                    return NoteListViewModel(noteRepository, notesAmt) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}