package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ViewEventViewModel(val eventDao: EventDao, private val eventID: Long): ViewModel() {
    var event: MutableLiveData<Event> = MutableLiveData<Event>()
    val petNavigator: Navigator = Navigator()
    //var petsAssociated: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    val petsAssociated = MutableLiveData<List<PetWithProfilePic>>()
    var eventDate: MutableLiveData<LocalDate> = MutableLiveData<LocalDate>()
    var eventTime: MutableLiveData<LocalTime> = MutableLiveData<LocalTime>()

    init {
        fetchEvent()
        Fetcher.fetchPetsOfEventWithProfilePhotos(viewModelScope, petsAssociated, eventDao, eventID)
    }

    private fun fetch() {
        fetchEvent()
        //fetchEventPets()
    }

    private fun fetchEvent() {
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

    /*private fun fetchEventPets() {
        viewModelScope.launch {
            val petsFetching = async { eventDao.getPetsOfEvent(eventID) }
            val fetchedPets = mutableListOf<Pet>()
            for (pet in petsFetching.await()) {
                fetchedPets.add(pet)
                //Log.e("eventPets 1/2", "added pet $pet")
            }
            petsAssociated.value = fetchedPets
            //Log.e("eventPets 2/2", "current pet list ${petsAssociated.value?.toString()}")
        }
    }*/
}