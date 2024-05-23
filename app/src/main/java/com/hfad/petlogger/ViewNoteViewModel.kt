package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ViewNoteViewModel(private val noteRepository: NoteRepository, private val noteId: Long) : ViewModel() {
    val note =  MutableLiveData<Note>()

    init {
        viewModelScope.launch {
            val fetchedNote = async {
                noteRepository.getNote(noteId)
            }
            note.value = fetchedNote.await()
        }
    }

    companion object {
        fun provideFactory(noteRepository: NoteRepository, noteId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewNoteViewModel::class.java)) {
                    return ViewNoteViewModel(noteRepository, noteId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}