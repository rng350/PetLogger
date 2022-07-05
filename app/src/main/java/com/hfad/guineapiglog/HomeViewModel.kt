package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(val petDao: PetDao, val eventDao: EventDao, val weightDao: WeightDao) : ViewModel() {
    var pets = petDao.getAll()
    var events = eventDao.getAll()
    var weights: MutableLiveData<MutableList<Weight>> = MutableLiveData(mutableListOf<Weight>())
    val petNavigator = Navigator()
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()

    init {
        Fetcher.fetchWeights(viewModel=this, weightsList=weights, weightDao=weightDao)
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