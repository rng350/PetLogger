package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.dao.EventDao
import com.hfad.guineapiglog.dao.PetDao
import com.hfad.guineapiglog.dao.WeightDao
import com.hfad.guineapiglog.entities.Event
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.PetWithProfilePic
import com.hfad.guineapiglog.entities.WeightWithPetName
import com.hfad.guineapiglog.fetchers.Fetcher
import com.hfad.guineapiglog.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(val petDao: PetDao, val eventDao: EventDao, val weightDao: WeightDao) : ViewModel() {
    val pets = MutableLiveData<List<PetWithProfilePic>>()
    var events = MutableLiveData<List<Event>>()
    var weights: MutableLiveData<MutableList<WeightWithPetName>> = MutableLiveData(mutableListOf<WeightWithPetName>())
    val petNavigator = Navigator()
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            pets.postValue(Fetcher.fetchPetsWithProfilePhotos(petDao))
            Log.d("HomeVM", "Pets fetched in VM")
        }
        viewModelScope.launch(Dispatchers.IO) {
            events.postValue(Fetcher.fetchAllEvents(eventDao))
            Log.d("HomeVM", "Events fetched in VM")
        }

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