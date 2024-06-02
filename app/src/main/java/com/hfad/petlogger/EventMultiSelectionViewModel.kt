package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import kotlinx.coroutines.launch

class EventMultiSelectionViewModel(private val eventRepository: EventRepository, private val initialSelection: List<Event>) : ViewModel() {
    private val _allEvents = MutableLiveData<List<CheckableItem<Event>>>()
    val allEvents get() = _allEvents
    val currentSelection = MutableLiveData<List<CheckableItem<Event>>>()
    val selectionTracker = EditSelectionTracker<Event>()
    private lateinit var fetchedAllEvents: List<Event>
    init {
        viewModelScope.launch {
            fetchedAllEvents = eventRepository.getAll()
            resetSelection()
        }
    }
    fun resetSelection() {
        val currentSelectionTemp = mutableListOf<CheckableItem<Event>>()
        val selectionTrackerInitialList = mutableListOf<Event>()
        _allEvents.value = fetchedAllEvents.map {
            if (initialSelection.contains(it)) {
                val checkableEvent = CheckableItem(it, isChecked = MutableLiveData(true))
                currentSelectionTemp.add(checkableEvent)
                selectionTrackerInitialList.add(it)
                checkableEvent
            } else CheckableItem(it)
        }
        currentSelection.value = currentSelectionTemp.toList()
        selectionTracker.initializeSelection(selectionTrackerInitialList.toList())
    }

    fun getEventsToAdd(): List<Event> {
        return selectionTracker.selectionToAdd.value?.map {
            it.item
        } ?: listOf<Event>()
    }

    fun getEventsToRemove(): List<Event> {
        return selectionTracker.selectionToRemove.value?.map {
            it.item
        } ?: listOf<Event>()
    }

    override fun onCleared() {
        super.onCleared()
        // destroy all observers
    }
    companion object {
        fun provideFactory(eventRepository: EventRepository, initialSelection: List<Event> = listOf<Event>()): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventMultiSelectionViewModel::class.java)) {
                    return EventMultiSelectionViewModel(eventRepository, initialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}