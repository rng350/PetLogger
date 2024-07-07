package com.hfad.petlogger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.repositories.EventRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditEventViewModel(private val eventRepository: EventRepository, eventID: Long): ViewModel() {
    val event = MutableLiveData<Event>()
    val eventDateTime = SelectableDateTime()
    val newEventTitle = MutableLiveData<String>()
    val newEventDetails = MutableLiveData<String>()
    private val _goToEventsList = MutableLiveData(false)
    val goToEventsList: LiveData<Boolean> get() = _goToEventsList

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
        petsToAdd: List<Pet> = listOf(),
        petsToRemove: List<Pet> = listOf(),
        photosToAdd: List<Photo> = listOf(),
        photosToRemove: List<Photo> = listOf()
    ) {
        if (newEventTitle.value?.isNotEmpty() == true) {
            event.value?.let { updatedEvent ->
                updatedEvent.title = newEventTitle.value!!
                updatedEvent.details = newEventDetails.value ?: ""
                updatedEvent.date = eventDateTime.dateTime
                viewModelScope.launch {
                    eventRepository.update(
                        event = updatedEvent,
                        petsToAdd = petsToAdd,
                        petsToRemove = petsToRemove,
                        photosToAdd = photosToAdd,
                        photosToRemove = photosToRemove
                    )
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