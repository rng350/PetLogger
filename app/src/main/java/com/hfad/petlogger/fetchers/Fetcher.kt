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

    fun fetchWeightPrevWeightAndPet(coroutineScope: CoroutineScope,
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