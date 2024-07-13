package com.hfad.petlogger

import androidx.lifecycle.*
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.entities.Event
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class ViewEventViewModel(val eventDao: EventDao, private val eventID: Long): ViewModel() {
    var event: MutableLiveData<Event> = MutableLiveData<Event>()
    var eventDate: MutableLiveData<LocalDate> = MutableLiveData<LocalDate>()
    var eventTime: MutableLiveData<LocalTime> = MutableLiveData<LocalTime>()

    init {
        viewModelScope.launch {
            val eventFetching = async { eventDao.get(eventID) }
            eventFetching.await().let {
                event.value = it
                val eventLocalDate = event.value?.date?.toLocalDateTime()
                eventDate.value = eventLocalDate?.toLocalDate()
                eventTime.value = eventLocalDate?.toLocalTime()
                // TODO: add nicer-looking local date & time display
            }
        }
    }

    companion object {
        fun provideFactory(eventDao: EventDao, eventID: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewEventViewModel::class.java)) {
                    return ViewEventViewModel(eventDao, eventID) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}