package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.photodisplay.stateless.GetAssociatedItemsUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EventMultiSelectionViewModel(
    private val eventRepository: EventRepository,
    private val getAssociatedEvents: GetAssociatedItemsUseCase<Event>?
) : ViewModel() {
    private val _allEvents = MutableLiveData<List<CheckableItem<Event>>>()
    private val initialSelection = HashSet<Event>()
    val allEvents get() = _allEvents
    val currentSelection = MutableLiveData<List<CheckableItem<Event>>>()
    val selectionTracker = EditSelectionTracker<Event>()
    private lateinit var fetchedAllEvents: List<Event>
    init {
        viewModelScope.launch {
            val initialWeightsFetch = async {
                getAssociatedEvents?.let { getInitialEvents ->
                    val initialEvents = getInitialEvents()
                    initialSelection.addAll(initialEvents)
                    selectionTracker.initializeSelection(initialEvents)
                }
            }
            fetchedAllEvents = async {
                eventRepository.getAll()
            }.await()
            initialWeightsFetch.await()
            resetSelection()
        }
    }
    fun resetSelection() {
        val currentSelectionTemp = mutableListOf<CheckableItem<Event>>()
        _allEvents.value = fetchedAllEvents.map {
            if (initialSelection.contains(it)) {
                val checkableEvent = CheckableItem(it, isChecked = MutableLiveData(true))
                currentSelectionTemp.add(checkableEvent)
                checkableEvent
            } else CheckableItem(it)
        }
        currentSelection.value = currentSelectionTemp.toList()
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

    companion object {
        fun provideFactory(eventRepository: EventRepository, getAssociatedWeights: GetAssociatedItemsUseCase<Event>? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventMultiSelectionViewModel::class.java)) {
                    return EventMultiSelectionViewModel(eventRepository, getAssociatedWeights) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}