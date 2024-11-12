package com.hfad.petlogger.screens.note.newnote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.notes.NoteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewNoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    // TODO: Implement the ViewModel
    var noteTitle: String = ""
    var noteDetails: String = ""
    private val _goBack = MutableLiveData<Boolean>(false)
    val goBack: LiveData<Boolean> = _goBack
    fun submitNote(pets: List<Long> = listOf(),
                   events: List<Long> = listOf(),
                   weights: List<Weight> = listOf(),
                   photos: List<Photo> = listOf(),
                   tags: List<Tag> = listOf()) {
        if (noteTitle.isNotEmpty() || noteDetails.isNotEmpty()) {
            val note = Note(title = noteTitle, details = noteDetails)
            viewModelScope.launch {
                val inserted = async {
                    noteRepository.insertNote(note, pets, events, weights, photos, tags)
                }
                inserted.await()
                _goBack.value = true
            }
        }
    }

    fun clear() {
        noteTitle = ""
        noteDetails = ""
    }

    companion object {
        fun provideFactory(noteRepository: NoteRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NewNoteViewModel::class.java)) {
                    return NewNoteViewModel(noteRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}