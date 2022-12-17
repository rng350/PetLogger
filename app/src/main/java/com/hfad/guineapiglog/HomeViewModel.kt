package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.entities.Event
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.PetWithProfilePic
import com.hfad.guineapiglog.entities.WeightWithPetName
import com.hfad.guineapiglog.fetchers.Fetcher
import kotlinx.coroutines.launch

class HomeViewModel(val petDao: PetDao, val eventDao: EventDao, val weightDao: WeightDao) : ViewModel() {
    //var pets: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    val pets = MutableLiveData<List<PetWithProfilePic>>()
    var events: MutableLiveData<MutableList<Event>> = MutableLiveData(mutableListOf<Event>())
    var weights: MutableLiveData<MutableList<WeightWithPetName>> = MutableLiveData(mutableListOf<WeightWithPetName>())
    val petNavigator = Navigator()
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()

    init {
        //Fetcher.fetchAllPets(this, pets, petDao)
        Fetcher.fetchPetsWithProfilePhotos(viewModelScope, pets, petDao)
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