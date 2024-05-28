package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.util.GetDateTimeDisplayUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ViewNoteViewModel(private val noteRepository: NoteRepository, private val noteId: Long) : ViewModel() {
    val note =  MutableLiveData<Note>()
    val lastUpdatedDateDisplay = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            val notePetsTempDELETE = async {
                noteRepository.getPetsOfNote(noteId)
            }
            val fetchedNote = async {
                noteRepository.getNote(noteId)
            }
            note.value = fetchedNote.await()
            note.value?.let {
                val dateTimeDisplayUseCase = GetDateTimeDisplayUseCase()
                lastUpdatedDateDisplay.value = dateTimeDisplayUseCase(it.lastUpdated)
            }
            val petsOfNoteDELETE = notePetsTempDELETE.await()
            Log.d("ViewNoteVM", "Pets_of_Note: ${petsOfNoteDELETE}")
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