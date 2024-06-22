package com.hfad.petlogger

import androidx.lifecycle.*
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.*
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.photodisplay.stateless.GetAssociatedItemsUseCase
import com.hfad.petlogger.repositories.EventRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.selectiontracker.NewSelectionTracker
import kotlinx.coroutines.launch

class EditPetViewModel(
    val petRepository: PetRepository,
    val petID: Long,
    val petDao: PetDao,
    val photoDao: PhotoDao,
    val eventDao: EventDao,
    val weightDao: WeightDao
): ViewModel() {
    val pet = MutableLiveData<Pet>()
    val events = MutableLiveData<MutableList<CheckableItem<Event>>>()
    val weights = MutableLiveData<MutableList<CheckableItem<Weight>>>()
    val petProfilePic = MutableLiveData<Photo>()
    val newPetProfilePic = MutableLiveData<Photo>()

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

    fun updatePet(
        eventsToRemove: List<Event> = listOf<Event>(),
        eventsToAdd: List<Event> = listOf<Event>(),
        weightsToRemove: List<Weight> = listOf<Weight>(),
        photosToAdd: List<Photo> = listOf<Photo>(),
        photosToRemove: List<Photo> = listOf<Photo>()
    ) {
        val editedPet = pet.value!!
        editedPet.petDOB = newPetDOB.dateTime
        editedPet.hasDOB = newPetDOB.hasBeenSet

        viewModelScope.launch {
            petDao.update(editedPet)
        }
        for (event in eventsToRemove) {
            viewModelScope.launch {
                petDao.delete(EventPet(event.eventId, petID))
            }
        }
        for (event in eventsToAdd) {
            viewModelScope.launch {
                petDao.insert(EventPet(event.eventId, petID))
            }
        }
        for (weight in weightsToRemove) {
            viewModelScope.launch {
                weightDao.delete(weight)
            }
        }
        viewModelScope.launch {
            petRepository.addPetPhotos(petID, photosToAdd)
        }
        for (photo in photosToRemove) {
            viewModelScope.launch {
                petDao.deletePetPhoto(PetPhoto(petID, photo.id))
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

    companion object {
        fun provideFactory(petRepository: PetRepository, petID: Long, petDao: PetDao, photoDao: PhotoDao, eventDao: EventDao, weightDao: WeightDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditPetViewModel::class.java)) {
                    return EditPetViewModel(petRepository, petID, petDao, photoDao, eventDao, weightDao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}