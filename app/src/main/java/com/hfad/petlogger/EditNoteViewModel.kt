package com.hfad.petlogger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.NoteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class EditNoteViewModel(private val noteRepository: NoteRepository, private val noteId: Long) : ViewModel() {
    private val noteFetched = MutableLiveData<Note>()
    val note = MutableLiveData<Note>()

    val goBack = MutableLiveData(false)
    private val _goToNotesList = MutableLiveData(false)
    val goToNotesList: LiveData<Boolean> get() = _goToNotesList
    init {
        viewModelScope.launch {
            noteFetched.value = noteRepository.getNote(noteId)
            noteFetched.value?.let {
                note.value = it.copy()
            }
        }
    }

    fun submitChanges(
        eventsToRemove: List<Event> = listOf<Event>(),
        eventsToAdd: List<Event> = listOf<Event>(),
        petsToAdd: List<Pet> = listOf(),
        petsToRemove: List<Pet> = listOf(),
        photosToAdd: List<Photo> = listOf(),
        photosToRemove: List<Photo> = listOf()
    ) {
        note.value?.let {
            viewModelScope.launch {
                async {
                    noteRepository.updateNote(
                        note = it.copy(lastUpdated = OffsetDateTime.now()),
                        eventsToAdd = eventsToAdd,
                        eventsToRemove = eventsToRemove,
                        petsToAdd = petsToAdd,
                        petsToRemove = petsToRemove,
                        photosToRemove = photosToRemove,
                        photosToAdd = photosToAdd
                    )
                }.await()
                goBack.value = true
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            note.value?.let {
                async {
                    noteRepository.delete(it)
                }.await()
                _goToNotesList.value = true
            }
        }
    }

    fun reset() {
        noteFetched.value?.let {
            note.value = it.copy()
        }
    }

    fun onNavigateToNotesList() {
        _goToNotesList.value = false
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