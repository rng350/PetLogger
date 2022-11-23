package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class EditEventViewModel(_eventID: Long, eventDao: EventDao, petDao: PetDao): ViewModel() {
    val event = MutableLiveData<Event>()
    val allPets = MutableLiveData<List<PetWithProfilePic>>() // yuck, wanna remove this...
    val initialPetSelection = MutableLiveData<List<PetWithProfilePic>>() // yuck, wanna remove this...
    val petsAssociated = SelectionEditTracker<PetWithProfilePic>(null)
    val pets = MutableLiveData<List<CheckableItem<PetWithProfilePic>>>()
    val eventID = MutableLiveData<Long>(_eventID)
    val eventDateTime = SelectableDateTime()
    val initialPhotoSelection = MutableLiveData<List<Photo>>()
    val photosAssociated = SelectionEditTracker<Photo>(10)

    var allPetsFetched = false
    var associatedPetsFetched = false

    init {
        Fetcher.fetchEvent(this, event, eventDao, _eventID)
        Fetcher.fetchPetsWithProfilePhotos(viewModelScope, allPets, petDao)
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
                petList.add(CheckableItem(it, petsAssociated.shouldBeChecked(it)))
            }
            Log.e("recyc view init", "about to initialize recyc view 2/3")
            pets.value = petList
            Log.e("recyc view init", "about to initialize recyc view 3/3 values put in. Vals:\n${petList}")
        }
    }

    fun initRecyclerViewPhotosList() {

    }

    fun submitChanges() {
    }
}