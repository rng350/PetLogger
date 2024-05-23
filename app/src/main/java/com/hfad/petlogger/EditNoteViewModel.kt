package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class EditNoteViewModel(private val noteRepository: NoteRepository, private val noteId: Long) : ViewModel() {
    private val noteFetched = MutableLiveData<Note>()
    val note = MutableLiveData<Note>()

    val goBack = MutableLiveData(false)
    init {
        viewModelScope.launch {
            noteFetched.value = noteRepository.getNote(noteId)
            noteFetched.value?.let {
                note.value = it.copy()
            }
        }
    }

    fun submitChanges() {
        if (note.value?.equals(noteFetched.value) == false) {
            note.value?.let {
                viewModelScope.launch {
                    async {
                        noteRepository.updateNote(it.copy(lastUpdated = OffsetDateTime.now()))
                    }.await()
                    goBack.value = true
                }
            }
        }
    }

    fun reset() {
        noteFetched.value?.let {
            note.value = it.copy()
        }
    }

    companion object {
        fun provideFactory(noteRepository: NoteRepository, noteId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
                    return EditNoteViewModel(noteRepository, noteId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}