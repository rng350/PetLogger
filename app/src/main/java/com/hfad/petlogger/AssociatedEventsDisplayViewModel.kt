package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetTimeDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedEventsDisplayViewModel(private val getAssociatedEvents: GetItemsUseCase<Event>): ViewModel() {
    val dateDisplay = GetDateDisplayUseCase()
    val timeDisplay = GetTimeDisplayUseCase()
    private val _events: MutableStateFlow<List<EventForList>> = MutableStateFlow<List<EventForList>>(listOf())
    val events: StateFlow<List<EventForList>> = _events.asStateFlow()
    val eventNavigator = Navigator()
    private var isLoading: Boolean = false

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedEvents = getAssociatedEvents().map { EventForList(it.eventId, dateDisplay(it.date), timeDisplay(it.date), it.title) }
            Log.d("AssocEventsVM", "Loaded Events Size: ${loadedEvents.size}")
            Log.d("AssocEventsVM", "List Size Before: ${events.value.size}")
            _events.update { it + loadedEvents }
            Log.d("AssocEventsVM", "List Size After: ${events.value.size}")
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAssociatedEvents.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    companion object {
        fun provideFactory(getAssociatedEvents: GetItemsUseCase<Event>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedEventsDisplayViewModel::class.java)) {
                    return AssociatedEventsDisplayViewModel(getAssociatedEvents) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}