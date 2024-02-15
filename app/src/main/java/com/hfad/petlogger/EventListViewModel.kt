package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventListViewModel(val eventDao: EventDao) : ViewModel() {
    var events = MutableLiveData<List<Event>>()
    val eventNavigator = Navigator()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            events.postValue(Fetcher.fetchAllEvents(eventDao))
            Log.d("HomeVM", "Events fetched in VM")
        }
    }
}