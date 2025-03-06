package com.hfad.petlogger.screens.event.newevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.datetimeselection.SelectableDateTime
import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.tags.data.Tag
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