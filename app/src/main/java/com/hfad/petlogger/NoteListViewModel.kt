package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.photodisplay.stateful.GetAllNotesForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteListViewModel(private val getAllNotes: GetItemsUseCase<Note>) : ViewModel() {
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
            val loadedNotes = getAllNotes()
            _notes.update { it + loadedNotes }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAllNotes.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    companion object {
        fun provideFactory(getAllNotes: GetItemsUseCase<Note>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
                    return NoteListViewModel(getAllNotes) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}