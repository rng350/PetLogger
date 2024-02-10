package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator
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