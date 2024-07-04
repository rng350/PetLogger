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

    suspend fun fetchAllPets(petDao: PetDao) = withContext(Dispatchers.IO) {
       petDao.getAll()
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