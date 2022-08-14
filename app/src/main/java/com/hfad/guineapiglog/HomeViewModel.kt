package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(val petDao: PetDao, val eventDao: EventDao, val weightDao: WeightDao) : ViewModel() {
    var pets: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    var events: MutableLiveData<MutableList<Event>> = MutableLiveData(mutableListOf<Event>())
    var weights: MutableLiveData<MutableList<WeightWithPetName>> = MutableLiveData(mutableListOf<WeightWithPetName>())
    val petNavigator = Navigator()
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()

    init {
        Fetcher.fetchAllPets(this, pets, petDao)
        Fetcher.fetchAllEvents(this, events, eventDao)
        Fetcher.fetchAllWeightsWithPetNames(this, weights, weightDao, petDao)
    }

    fun deletePet(pet: Pet) {
        viewModelScope.launch {
            petDao.delete(pet)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.delete(event)
        }
    }
}