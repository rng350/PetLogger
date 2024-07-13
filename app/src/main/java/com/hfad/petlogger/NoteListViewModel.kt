package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.photodisplay.stateful.GetAllNotesForDisplayUseCase
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteListViewModel(getAllNotes: GetAllNotesForDisplayUseCase) : ViewModel() {
    val notes = getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf()
    )
    val noteNavigator = Navigator()

    companion object {
        fun provideFactory(getAllNotes: GetAllNotesForDisplayUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
                    return NoteListViewModel(getAllNotes) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}