package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteListViewModel(noteRepository: NoteRepository) : ViewModel() {
    val notes = MutableLiveData<List<Note>>()
    val noteNavigator = Navigator()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            notes.postValue(noteRepository.getAllNotes())
        }
    }

    companion object {
        fun provideFactory(noteRepository: NoteRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
                    return NoteListViewModel(noteRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}