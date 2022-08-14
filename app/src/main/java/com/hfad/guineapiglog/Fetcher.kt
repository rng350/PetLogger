package com.hfad.guineapiglog

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object Fetcher {
    fun fetchPet(viewModel: ViewModel, associatedPet: MutableLiveData<Pet>, petDao: PetDao, petID: Long) {
        viewModel.viewModelScope.launch {
            var fetchedPet = async {
                petDao.getAsync(petID)
            }
            associatedPet.value = fetchedPet.await()
        }
    }

    fun fetchAllPets(viewModel: ViewModel, petsList: MutableLiveData<MutableList<Pet>>, petDao: PetDao) {
        viewModel.viewModelScope.launch {
            var fetchedPets = async {
                petDao.getAll()
            }
            petsList.value = fetchedPets.await()
        }
    }

    fun fetchPetsOfEvent(viewModel: ViewModel, petsList: MutableLiveData<MutableList<Pet>>, eventDao: EventDao, eventID: Long) {
        viewModel.viewModelScope.launch {
            var fetchedPets = async {
                eventDao.getPetsOfEvent(eventID)
            }
            petsList.value = fetchedPets.await()
        }
    }

    fun fetchWeight(viewModel: ViewModel, associatedWeight: MutableLiveData<Weight>, weightDao: WeightDao, weightID: Long) {
        viewModel.viewModelScope.launch {
            var fetchedWeight = async {
                weightDao.get(weightID)
            }
            associatedWeight.value = fetchedWeight.await()
        }
    }

    fun fetchAllWeights(viewModel: ViewModel, weightsList: MutableLiveData<MutableList<Weight>>, weightDao: WeightDao) {
        viewModel.viewModelScope.launch {
            val fetchedWeights = async {
                weightDao.getAll()
            }
            weightsList.value = fetchedWeights.await()
        }
    }

    fun fetchAllWeightsWithPetNames(viewModel: ViewModel, weightsList: MutableLiveData<MutableList<WeightWithPetName>>, weightDao: WeightDao, petDao: PetDao) {
        viewModel.viewModelScope.launch {
            val fetchedWeights = async {
                weightDao.getAll()
            }
            val fetchedPets = async {
                petDao.getAll()
            }
            val petIDNameMap = HashMap<Long, String>()
            val pets = fetchedPets.await()
            for (pet in pets) {
                petIDNameMap[pet.petID] = pet.petName
            }
            weightsList.value =
                fetchedWeights.await().map { WeightWithPetName(it, petIDNameMap[it.petId] ?: "N/A") }
                    .toMutableList()
        }
    }

    fun fetchWeightsOfPet(viewModel: ViewModel, weightsList: MutableLiveData<MutableList<Weight>>, weightDao: WeightDao, petID: Long) {
        viewModel.viewModelScope.launch {
            val fetchedWeights = async {
                weightDao.getPetWeights(petID)
            }
            weightsList.value = fetchedWeights.await()
        }
    }

    fun fetchEvent(viewModel: ViewModel, associatedEvent: MutableLiveData<Event>, eventDao: EventDao, eventID: Long) {
        viewModel.viewModelScope.launch {
            val fetchedEvent = async {
                eventDao.get(eventID)
            }
            associatedEvent.value = fetchedEvent.await()
        }
    }

    fun fetchAllEvents(viewModel: ViewModel, eventsList: MutableLiveData<MutableList<Event>>, eventDao: EventDao) {
        viewModel.viewModelScope.launch {
            val fetchedEvents = async {
                eventDao.getAll()
            }
            eventsList.value = fetchedEvents.await()
        }
    }

    fun fetchEventsOfPet(viewModel: ViewModel, eventsList: MutableLiveData<MutableList<Event>>, petDao: PetDao, petID: Long) {
        viewModel.viewModelScope.launch {
            val fetchedEvents = async {
                petDao.getEventsOfPet(petID)
            }
            eventsList.value = fetchedEvents.await()
        }
    }
}