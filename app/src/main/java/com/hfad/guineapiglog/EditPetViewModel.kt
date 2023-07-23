package com.hfad.guineapiglog

import androidx.lifecycle.*
import com.hfad.guineapiglog.dao.EventDao
import com.hfad.guineapiglog.dao.PetDao
import com.hfad.guineapiglog.dao.PhotoDao
import com.hfad.guineapiglog.dao.WeightDao
import com.hfad.guineapiglog.entities.*
import com.hfad.guineapiglog.fetchers.Fetcher
import com.hfad.guineapiglog.selectiontracker.NewSelectionTracker
import kotlinx.coroutines.launch

class EditPetViewModel(val petID: Long, val petDao: PetDao, val photoDao: PhotoDao, val eventDao: EventDao, val weightDao: WeightDao): ViewModel() {
    val pet = MutableLiveData<Pet>()
    //val pet = petDao.get(petID)
    val events = MutableLiveData<MutableList<CheckableItem<Event>>>()
    val weights = MutableLiveData<MutableList<CheckableItem<Weight>>>()
    val petProfilePic = MutableLiveData<Photo>()
    val newPetProfilePic = MutableLiveData<Photo>()
    val newPetProfilePicLocal = MutableLiveData<Photo>()
    val weightsToRemove = NewSelectionTracker<Weight>(choiceLimit = null)
    val eventsToRemove = NewSelectionTracker<Event>(choiceLimit = null)
    val photoSelection = NewSelectionTracker<Photo>(1)
    val _petID = MutableLiveData<Long>(petID)

    val newPetSex = MutableLiveData<String>("N/A")
    var newPetDOB = SelectableDateOptional()

    init {
        Fetcher.fetchPet(this, pet, petDao, petID)
        Fetcher.fetchPetProfilePhoto(viewModelScope, petProfilePic, petDao, petID)
        Fetcher.fetchCheckableEventsOfPet(viewModelScope, events, petDao, petID)
        Fetcher.fetchCheckableWeightsOfPet(viewModelScope, weights, petDao, petID)
    }

    fun onPetFetched() {
        val (_, _, _, _, petSex, petDOB, _) = pet.value!!
        newPetSex.value = petSex
        newPetDOB.set(petDOB)
    }

    fun toggleWeight(checkableWeight: CheckableItem<Weight>) {
        weightsToRemove.toggle(checkableWeight)
    }

    fun toggleEvent(checkableEvent: CheckableItem<Event>) {
        eventsToRemove.toggle(checkableEvent)
    }

    fun submitChanges() {
        // update pet
        updatePet()
        // go back to view pet page
    }

    fun updatePet() {
        val editedPet = pet.value!!
        editedPet.petDOB = newPetDOB.dateTime
        editedPet.hasDOB = newPetDOB.hasBeenSet

        viewModelScope.launch {
            petDao.update(editedPet)
        }
        for (event in eventsToRemove.selectionToAdd.value!!) {
            viewModelScope.launch {
                petDao.delete(EventPet(event.item.eventId, petID))
            }
        }
        for (weight in weightsToRemove.selectionToAdd.value!!) {
            viewModelScope.launch {
                weightDao.delete(weight.item)
            }
        }
    }

    fun removeDOB() {
        newPetDOB.hasBeenSet = false
    }

    // call after "yes" on "are you sure?"
    fun deletePet() {
        // TODO: implement
        // delete pet
        // delete pet photo
        deleteProfilePhoto()
        // go back to home screen
    }

    fun deleteProfilePhoto() {
        petProfilePic.value?.let {
            viewModelScope.launch {
                photoDao.delete(PetProfilePhoto(photoID = it.id, petID = petID))
            }
        }
        // delete photo from local storage
    }

    fun setPetSex(newPetSex: String) {
        pet.value?.petSex = newPetSex
    }
}