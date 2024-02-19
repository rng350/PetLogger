package com.hfad.petlogger.fetchers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.util.Converter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Fetcher {
    fun fetchPet(viewModel: ViewModel, associatedPet: MutableLiveData<Pet>, petDao: PetDao, petID: Long) {
        viewModel.viewModelScope.launch {
            var fetchedPet = async {
                petDao.getAsync(petID)
            }
            associatedPet.value = fetchedPet.await()
        }
    }

    suspend fun fetchAllPets(petDao: PetDao) = withContext(Dispatchers.IO) {
       petDao.getAll()
    }

    fun fetchPetsOfEvent(viewModel: ViewModel, petsList: MutableLiveData<MutableList<Pet>>, eventDao: EventDao, eventID: Long) {
        viewModel.viewModelScope.launch {
            var fetchedPets = async {
                eventDao.getPetsOfEvent(eventID)
            }
            petsList.value = fetchedPets.await()
        }
    }

    fun fetchWeight(coroutineScope: CoroutineScope,
                    associatedWeight: MutableLiveData<Weight>,
                    associatedPet: MutableLiveData<Pet>,
                    prevWeight: MutableLiveData<Weight?>,
                    weightDao: WeightDao,
                    petDao: PetDao,
                    weightID: Long) {
        coroutineScope.launch {
            var fetchedWeight = async {
                weightDao.get(weightID)
            }
            associatedWeight.value = fetchedWeight.await()

            associatedWeight.value?.let { weight ->
                coroutineScope.launch {
                    Converter.fromOffsetDateTime(weight.weightDateTime)?.let { dateTime ->
                        val fetchedPrevWeight = async {
                            weightDao.getPreviousWeight(weight.petId, dateTime)
                        }
                        prevWeight.value = fetchedPrevWeight.await()
                        Log.i("fetchWeight","PrevWeight: ${prevWeight.value?.weightGrams ?: "N/A"}")
                    }
                }
                coroutineScope.launch {
                    val fetchedAssociatedPet = async {
                        petDao.getPet(weight.petId)
                    }
                    associatedPet.value = fetchedAssociatedPet.await()
                    Log.i("fetchWeight","Pet: ${associatedPet.value?.petName ?: "N/A"}")
                }
            }
        }
    }

    /*fun fetchWeightPrevWeightAndPet(coroutineScope: CoroutineScope,
                                    weight: MutableLiveData<Weight>,
                                    previousWeight: MutableLiveData<Weight?>,
                                    associatedPet: MutableLiveData<Pet>,
                                    weightID: Long, weightDao: WeightDao, petDao: PetDao) {
        coroutineScope.launch(Dispatchers.IO) {
            Log.i("fetchWeightPrev", "calling function")
            val fetchedWeight = async {
                Log.i("fetchWeightPrev", "got weight 1")
                weightDao.get(weightID)
            }
            Log.i("fetchWeightPrev", "got weight 2")
            weight.value = fetchedWeight.await()
            Log.i("fetchWeightPrev", "got weight 3")
            weight.value?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    *//*Converter.fromOffsetDateTime(it.weightDateTime)?.let {
                        val fetchedPrevWeight = async {
                            weightDao.getPreviousWeight(it)
                        }
                        previousWeight.value = fetchedPrevWeight.await()
                    }*//*
                }
                coroutineScope.launch(Dispatchers.IO) {
                    val fetchedAssociatedPet = async {
                        petDao.getPet(it.petId)
                    }
                    associatedPet.value = fetchedAssociatedPet.await()
                    Log.i("fetchWeightPrev", "got pet")
                }
            }
        }
    }*/
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

    suspend fun fetchAllEvents(eventDao: EventDao) = withContext(Dispatchers.IO) {
        val eventsFetched = eventDao.getAll()
            .sortedByDescending { it.date }
        Log.d("Fetcher", "Fetched events: $eventsFetched")
        eventsFetched
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

    suspend fun fetchPetsWithProfilePhotos(petDao: PetDao): List<PetWithProfilePic> = withContext(Dispatchers.IO) {
        val fetchedPets = petDao.getAllPetsWithProfilePhotos()
        Log.d("Fetcher", "fetched pets: ${fetchedPets}")
        fetchedPets
    }

    fun fetchPetsOfEventWithProfilePhotos(coroutineScope: CoroutineScope,
                                          petsWithProfilePhotos: MutableLiveData<List<PetWithProfilePic>>,
                                          eventDao: EventDao,
                                          eventID: Long) {
        coroutineScope.launch {
            val fetchedPets = async { eventDao.getPetsOfEventWithProfilePhotos(eventID) }
            fetchedPets.await().let {
                petsWithProfilePhotos.value = it
            }
        }
    }

    fun fetchAllPhotos(coroutineScope: CoroutineScope,
                       photoList: MutableLiveData<List<Photo>>,
                       photoDao: PhotoDao) {
        coroutineScope.launch {
            val fetchedPhotos = async { photoDao.getAllPhotos() }
            fetchedPhotos.await().let {
                photoList.value = it
            }
        }
    }
}