package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.photodisplay.stateful.GetAllEventsForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateful.GetAllPetsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventListViewModel(getAllEvents: GetAllEventsForDisplayUseCase) : ViewModel() {
    val events: StateFlow<List<Event>> = getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf<Event>()
        )
    val eventNavigator = Navigator()

    companion object {
        fun provideFactory(getAllEvents: GetAllEventsForDisplayUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EventListViewModel::class.java)) {
                    return EventListViewModel(getAllEvents) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }

}