package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.*
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.EventPetDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventPet
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.selectiontracker.NewSelectionTracker
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewEventViewModel(private val eventRepository: EventRepository): ViewModel() {
    var eventTitle: String = ""
    var eventDetails: String = ""
    var eventDateTime = SelectableDateTime()
    private val _carryOn = MutableLiveData(false)
    val carryOn: LiveData<Boolean>
        get() = _carryOn

    fun submitEvent(
        pets: List<Long> = listOf<Long>(),
        photos: List<Photo> = listOf<Photo>(),
        notes: List<Note> = listOf<Note>()
    ) {
        viewModelScope.launch {
            if (eventTitle.isNotEmpty()) {
                async {
                    eventRepository.insert(
                        event = Event(title=eventTitle, details=eventDetails, date=eventDateTime.selectedDateTime),
                        pets = pets,
                        photos = photos,
                        notes = notes
                    )
                }.await()
                _carryOn.value = true
            }
        }
    }

    companion object {
        fun provideFactory(eventRepository: EventRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NewEventViewModel::class.java)) {
                    return NewEventViewModel(eventRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}