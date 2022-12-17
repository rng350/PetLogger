package com.hfad.guineapiglog.fetchers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.*
import com.hfad.guineapiglog.entities.*
import kotlinx.coroutines.CoroutineScope
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
            weightsList.value =
                fetchedWeights.await()
                    .sortedByDescending { it.weightDateTime }
                    .toMutableList()
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
                fetchedWeights.await()
                    .map { WeightWithPetName(it, petIDNameMap[it.petId] ?: "N/A") }
                    .sortedByDescending {it.weight.weightDateTime}
                    .toMutableList()

        }
    }

    fun fetchWeightsOfPet(coroutineScope: CoroutineScope, weightsList: MutableLiveData<MutableList<Weight>>, petDao: PetDao, petID: Long) {
        coroutineScope.launch {
            val fetchedWeights = async {
                petDao.getWeightsOfPet(petID)
            }
            weightsList.value =
                fetchedWeights.await()
                    .sortedByDescending { it.weightDateTime }
                    .toMutableList()
        }
    }

    fun fetchCheckableWeightsOfPet(coroutineScope: CoroutineScope, weightsList: MutableLiveData<MutableList<CheckableItem<Weight>>>, petDao: PetDao, petID: Long) {
        coroutineScope.launch {
            val fetchedWeights = async {
                petDao.getWeightsOfPet(petID)
            }
            val weights =
                fetchedWeights.await()
                    .sortedByDescending { it.weightDateTime }
                    .toMutableList()

            //put into new list
            val newList = mutableListOf<CheckableItem<Weight>>()
            weights.map{
                newList.add(CheckableItem(it))
            }

            weightsList.value = newList
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
            eventsList.value =
                fetchedEvents.await()
                    .sortedByDescending { it.date }
                    .toMutableList()
        }
    }

    fun fetchEventsOfPet(viewModel: ViewModel, eventsList: MutableLiveData<MutableList<Event>>, petDao: PetDao, petID: Long) {
        viewModel.viewModelScope.launch {
            val fetchedEvents = async {
                petDao.getEventsOfPet(petID)
            }
            eventsList.value =
                fetchedEvents.await()
                    .sortedByDescending { it.date }
                    .toMutableList()
        }
    }

    fun fetchCheckableEventsOfPet(coroutineScope: CoroutineScope, eventsList: MutableLiveData<MutableList<CheckableItem<Event>>>, petDao: PetDao, petID: Long) {
        coroutineScope.launch {
            // fill up blank list
            val fetchedEvents = async {
                petDao.getEventsOfPet(petID)
            }
            val regList =
                fetchedEvents.await()
                    .sortedByDescending { it.date }
                    .toMutableList()

            //put into new list
            val newList = mutableListOf<CheckableItem<Event>>()
            regList.map {
                newList.add(CheckableItem(it))
            }

            Log.e("fetch", "fetched checkable events of pet: ${newList}")
            eventsList.value = newList
        }

    }

    fun fetchPhotosOfEvent(viewModel: ViewModel, photosList: MutableLiveData<List<Photo>>, eventDao: EventDao, eventID: Long) {
        viewModel.viewModelScope.launch {
            val fetchedPhotos = async {
                eventDao.fetchPhotosOfEvent(eventID)
            }
            photosList.value =
                fetchedPhotos.await()
                    .sortedByDescending { it.date }
        }
    }

    fun fetchCheckablePhotosOfEvent(coroutineScope: CoroutineScope, checkablePhotosList: MutableLiveData<List<CheckableItem<Photo>>>, eventDao: EventDao, eventID: Long) {
        coroutineScope.launch {
            val fetchedPhotos = async {
                eventDao.fetchPhotosOfEvent(eventID)
            }
            val checkablePhotos = mutableListOf<CheckableItem<Photo>>()
            fetchedPhotos.await().map {
                checkablePhotos.add(CheckableItem(it))
            }
            checkablePhotosList.value = checkablePhotos.toList()
        }
    }

    fun fetchPetProfilePhoto(coroutineScope: CoroutineScope, profilePhoto: MutableLiveData<Photo>, petDao: PetDao, petID: Long) {
        coroutineScope.launch {
            val fetchedPhoto = async {
                petDao.getPetProfilePhoto(petID)
            }
            fetchedPhoto.await()?.let {
                profilePhoto.value = it
            }
        }
    }

    fun fetchPetsWithProfilePhotos(coroutineScope: CoroutineScope, petsWithProfilePhotos: MutableLiveData<List<PetWithProfilePic>>, petDao: PetDao) {
        coroutineScope.launch {
            val fetchedPets = async { petDao.getAllPetsWithProfilePhotos() }
            fetchedPets.await().let {
                Log.e("pets with pfp", "${it}")
                petsWithProfilePhotos.value = it
            }
        }
    }

    fun fetchPetsOfEventWithProfilePhotos(coroutineScope: CoroutineScope, petsWithProfilePhotos: MutableLiveData<List<PetWithProfilePic>>, eventDao: EventDao, eventID: Long) {
        coroutineScope.launch {
            val fetchedPets = async { eventDao.getPetsOfEventWithProfilePhotos(eventID) }
            fetchedPets.await().let {
                petsWithProfilePhotos.value = it
            }
        }
    }
}