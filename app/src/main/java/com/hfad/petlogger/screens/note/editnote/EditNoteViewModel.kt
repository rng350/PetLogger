package com.hfad.petlogger.screens.note.editnote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.notes.NoteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class EditNoteViewModel(private val noteRepository: NoteRepository, private val noteId: Long) : ViewModel() {
    private val _fetchedNote: MutableLiveData<Note> = MutableLiveData<Note>()
    val fetchedNote: LiveData<Note> get() = _fetchedNote
    val noteTitle = MutableLiveData<String>()
    val noteDetails = MutableLiveData<String>()

    val goBack = MutableLiveData(false)
    private val _goToNotesList = MutableLiveData(false)
    val goToNotesList: LiveData<Boolean> get() = _goToNotesList
    init {
        viewModelScope.launch {
            Log.d("EditNoteVM", "11111")
            val noteFetched = async {
                noteRepository.getNote(noteId)
            }
            _fetchedNote.value = noteFetched.await()
            Log.d("EditNoteVM", "22222, fetched Note: ${fetchedNote.value}")
            fetchedNote.value?.let {
                noteTitle.value = it.title
                noteDetails.value = it.details
                Log.d("EditNoteVM", "333333")
            }
            Log.d("EditNoteVM", "444444")
        }
    }

    fun submitChanges(
        eventsToRemove: List<Long> = listOf<Long>(),
        eventsToAdd: List<Long> = listOf<Long>(),
        petsToAdd: List<Long> = listOf(),
        petsToRemove: List<Long> = listOf(),
        photosToAdd: List<Photo> = listOf(),
        photosToRemove: List<Photo> = listOf(),
        tagsToAdd: List<Tag> = listOf(),
        tagsToRemove: List<Tag> = listOf()
    ) {
        if (noteTitle.value!=null && noteDetails.value!=null) {
            viewModelScope.launch {
                async {
                    noteRepository.updateNote(
                        note = Note(
                            id = noteId,
                            title = noteTitle.value ?: "",
                            details = noteDetails.value ?: "",
                            lastUpdated = OffsetDateTime.now()
                        ),
                        eventsToAdd = eventsToAdd,
                        eventsToRemove = eventsToRemove,
                        petsToAdd = petsToAdd,
                        petsToRemove = petsToRemove,
                        photosToRemove = photosToRemove,
                        photosToAdd = photosToAdd,
                        tagsToAdd = tagsToAdd,
                        tagsToRemove = tagsToRemove
                    )
                }.await()
                goBack.value = true
            }
        }
    }

    fun delete() {
        fetchedNote.value?.let {
            viewModelScope.launch {
                async {
                    noteRepository.delete(it)
                }.await()
                _goToNotesList.value = true
            }
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