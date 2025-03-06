package com.hfad.petlogger.screens.event.viewevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetTimeDisplayUseCase
import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.events.data.EventDao
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ViewEventViewModel(val eventDao: EventDao, private val eventID: Long): ViewModel() {
    private val _event: MutableLiveData<Event> = MutableLiveData<Event>()
    val event: LiveData<Event> get() = _event
    private val _eventDate: MutableLiveData<String> = MutableLiveData<String>()
    val eventDate: LiveData<String> get() = _eventDate
    private val _eventTime: MutableLiveData<String> = MutableLiveData<String>()
    val eventTime: LiveData<String> get() = _eventTime

    init {
        viewModelScope.launch {
            val eventFetching = async { eventDao.get(eventID) }
            eventFetching.await().let {
                _event.value = it
                val eventLocalDate = event.value?.date?.toLocalDateTime()
                _eventDate.value = GetDateDisplayUseCase().invoke(eventLocalDate)
                _eventTime.value = GetTimeDisplayUseCase().invoke(eventLocalDate)
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