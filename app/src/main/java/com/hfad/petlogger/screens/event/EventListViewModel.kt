package com.hfad.petlogger.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.events.EventForList
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.common.util.NewEntityNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventListViewModel(private val getAllEvents: GetItemsUseCase<EventForList>) : ViewModel() {
    private val _events: MutableStateFlow<List<EventForList>> = MutableStateFlow(listOf())
    val event: StateFlow<List<EventForList>> = _events.asStateFlow()
    val eventNavigator = Navigator()
    val newEventNavigator = NewEntityNavigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedEvents = getAllEvents()
            _events.update { it + loadedEvents }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAllEvents.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }
    companion object {
        fun provideFactory(getAllEvents: GetItemsUseCase<EventForList>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventListViewModel::class.java)) {
                    return EventListViewModel(getAllEvents) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }

}