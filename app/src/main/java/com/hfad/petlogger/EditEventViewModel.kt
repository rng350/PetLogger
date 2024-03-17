package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditEventViewModel(_eventID: Long, val eventDao: EventDao, petDao: PetDao): ViewModel() {
    val event = MutableLiveData<Event>()
    val allPets = MutableLiveData<List<PetWithProfilePic>>() // yuck, wanna remove this...
    val initialPetSelection = MutableLiveData<List<PetWithProfilePic>>() // yuck, wanna remove this...
    val petsAssociated = EditSelectionTracker<PetWithProfilePic>(null)
    val pets = MutableLiveData<List<CheckableItem<PetWithProfilePic>>>()
    val eventID = MutableLiveData<Long>(_eventID)
    val eventDateTime = SelectableDateTime()
    val initialPhotoSelection = MutableLiveData<List<Photo>>() // yuck, wanna remove this...
    val initialPhotos = MutableLiveData<List<CheckableItem<Photo>>>()

    var allPetsFetched = false
    var associatedPetsFetched = false

    init {
        Fetcher.fetchEvent(this, event, eventDao, _eventID)
        viewModelScope.launch(Dispatchers.IO) {
            allPets.postValue(Fetcher.fetchPetsWithProfilePhotos(petDao))
        }
        Fetcher.fetchPetsOfEventWithProfilePhotos(viewModelScope, initialPetSelection, eventDao, _eventID)
        Fetcher.fetchPhotosOfEvent(this, initialPhotoSelection, eventDao, _eventID)
    }

    fun onEventFetched() {
        eventDateTime.set(event.value!!.date)
    }

    fun initRecyclerViewPetList() {
        Log.e("recyc view init", "about to initialize recyc view 1/3\nall pets fetched (${allPetsFetched}), assoc pets fetched (${associatedPetsFetched})")
        if (allPetsFetched && associatedPetsFetched) {
            val petList = mutableListOf<CheckableItem<PetWithProfilePic>>()
            allPets.value?.map {
                petList.add(CheckableItem(it, MutableLiveData(petsAssociated.inInitialSelection(it))))
            }
            Log.e("recyc view init", "about to initialize recyc view 2/3")
            pets.value = petList
            Log.e("recyc view init", "about to initialize recyc view 3/3 values put in. Vals:\n${petList}")
        }
    }

    fun initAssociatedPhotoList() {
        initialPhotoSelection.value?.let { photos ->
            val oldPhotoList = mutableListOf<CheckableItem<Photo>>()
            photos.map {
                oldPhotoList.add(CheckableItem(it))
            }
            initialPhotos.value = oldPhotoList
        }
    }

    // TODO: Implement
    fun submitChanges() {
        event.value?.date = eventDateTime.dateTime
        event.value?.let {
            viewModelScope.launch {
                eventDao.update(it)
            }
        }
        petsAssociated.selectionToAdd.value?.let {
        }
        petsAssociated.selectionToRemove.value?.let {
        }
    }

    // TODO: Implement
    fun deleteEvent() {

    }
}