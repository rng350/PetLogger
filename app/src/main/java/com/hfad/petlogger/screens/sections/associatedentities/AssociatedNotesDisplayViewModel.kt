package com.hfad.petlogger.screens.sections.associatedentities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedNotesDisplayViewModel (private val getAssociatedNotes: GetItemsUseCase<Note>) : ViewModel() {
    private val _notes: MutableStateFlow<List<Note>> = MutableStateFlow<List<Note>>(listOf())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    val noteNavigator = Navigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedNotes = getAssociatedNotes()
            Log.d("AssocEventsVM", "Loaded Notes Size: ${loadedNotes.size}")
            Log.d("AssocEventsVM", "List Size Before: ${notes.value.size}")
            _notes.update { it + loadedNotes }
            Log.d("AssocEventsVM", "List Size After: ${notes.value.size}")
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAssociatedNotes.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    companion object {
        fun provideFactory(getAssociatedNotes: GetItemsUseCase<Note>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedNotesDisplayViewModel::class.java)) {
                    return AssociatedNotesDisplayViewModel(getAssociatedNotes) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}