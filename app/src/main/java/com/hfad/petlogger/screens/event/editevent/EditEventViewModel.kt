package com.hfad.petlogger.screens.event.editevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.SelectableDateTime
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.events.EventRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditEventViewModel(private val eventRepository: EventRepository, eventID: Long): ViewModel() {
    val event = MutableLiveData<Event>()
    val eventDateTime = SelectableDateTime()
    val newEventTitle = MutableLiveData<String>()
    val newEventDetails = MutableLiveData<String>()
    private val _goToEventsList = MutableLiveData(false)
    val goToEventsList: LiveData<Boolean> get() = _goToEventsList
    val eventDateDisplay: StateFlow<String> = eventDateTime.dateDisplay.asFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )
    val eventTimeDisplay: StateFlow<String> = eventDateTime.timeDisplay.asFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _goToViewEvent = MutableLiveData(false)
    val goToViewEvent: LiveData<Boolean> get() = _goToViewEvent

    init {
        viewModelScope.launch {
            val eventFetched = async {
                eventRepository.get(eventID)
            }
            event.value = eventFetched.await()
            newEventTitle.value = event.value!!.title
            newEventDetails.value = event.value!!.details
            eventDateTime.set(event.value!!.date)
        }
    }

    fun submitChanges(
        petsToAdd: List<Long> = listOf(),
        petsToRemove: List<Long> = listOf(),
        photosToAdd: List<Photo> = listOf(),
        photosToRemove: List<Photo> = listOf(),
        notesToAdd: List<Note> = listOf(),
        notesToRemove: List<Note> = listOf(),
        tagsToAdd: List<Tag> = listOf(),
        tagsToRemove: List<Tag> = listOf()
    ) {
        if (newEventTitle.value?.isNotEmpty() == true) {
            event.value?.let { updatedEvent ->
                updatedEvent.title = newEventTitle.value!!
                updatedEvent.details = newEventDetails.value ?: ""
                updatedEvent.date = eventDateTime.selectedDateTime
                viewModelScope.launch {
                    async {
                        eventRepository.update(
                            event = updatedEvent,
                            petsToAdd = petsToAdd,
                            petsToRemove = petsToRemove,
                            photosToAdd = photosToAdd,
                            photosToRemove = photosToRemove,
                            notesToAdd = notesToAdd,
                            notesToRemove = notesToRemove,
                            tagsToAdd = tagsToAdd,
                            tagsToRemove = tagsToRemove
                        )
                    }.await()
                    _goToViewEvent.value = true
                }
            }
        }
    }

    fun deleteEvent() {
        event.value?.let {
            viewModelScope.launch {
                async {
                    eventRepository.delete(it)
                }.await()
                _goToEventsList.value = true
            }
        }
    }

    fun onNavigateToEventsList() {
        _goToEventsList.value = false
    }

    fun onNavigateToViewEvent() {
        _goToViewEvent.value = false
    }

    companion object {
        fun provideFactory(eventRepository: EventRepository, eventID: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
                    return EditEventViewModel(eventRepository, eventID) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}