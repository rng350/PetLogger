package com.hfad.petlogger.screens.event.newevent

import androidx.lifecycle.*
import com.hfad.petlogger.common.SelectableDateTime
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.events.EventRepository
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
        notes: List<Note> = listOf<Note>(),
        tags: List<Tag> = listOf<Tag>()
    ) {
        viewModelScope.launch {
            if (eventTitle.isNotEmpty()) {
                async {
                    eventRepository.insert(
                        event = Event(title=eventTitle, details=eventDetails, date=eventDateTime.selectedDateTime),
                        pets = pets,
                        photos = photos,
                        notes = notes,
                        tags = tags
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