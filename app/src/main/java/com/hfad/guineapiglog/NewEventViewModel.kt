package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import java.time.OffsetDateTime
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NewEventViewModel(val eventDao: EventDao, val eventPetDao: EventPetDao): ViewModel() {
    var eventTitle: String = "N/A"
    var eventDetails: String = "N/A"
    var eventDateTime: OffsetDateTime = OffsetDateTime.now()
    var eventDateDisplay: String = "Event date"
    var eventTimeDisplay: String = "Event time"

    fun addEvent() {
        viewModelScope.launch {
            var event = Event(date = eventDateTime)
            event.title = eventTitle
            event.details = eventDetails
            eventDao.insert(event)
        }
    }
}