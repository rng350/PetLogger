package com.hfad.petlogger

import androidx.lifecycle.*
import com.hfad.petlogger.entities.*
import com.hfad.petlogger.repositories.PetRepository
import kotlinx.coroutines.launch

class EditPetViewModel(
    val petRepository: PetRepository,
    val petID: Long
): ViewModel() {
    val pet = MutableLiveData<Pet>()
    val petName = MutableLiveData<String>("")
    val petSpecies = MutableLiveData<String>("")
    val petBreed = MutableLiveData<String>("")

    val petSex = MutableLiveData<String>("N/A")
    var newPetDOB = SelectableDateOptional()
    private val _doneUpdating = MutableLiveData(false)
    val doneUpdating: LiveData<Boolean> get() = _doneUpdating
    val _goToPetList = MutableLiveData(false)
    val goToPetList: LiveData<Boolean> get() = _goToPetList

    init {
        viewModelScope.launch {
            launch {
                val retrievedPet = petRepository.getPet(petID)
                pet.postValue(retrievedPet)
                petName.postValue(retrievedPet.petName)
                petSpecies.postValue(retrievedPet.petSpecies)
                petBreed.postValue(retrievedPet.petBreed)
                petSex.postValue(retrievedPet.petSex)
                retrievedPet.petDOB?.let {
                    newPetDOB.set(it)
                }
            }
        }
    }

    fun updatePet(
        eventsToRemove: List<Event> = listOf<Event>(),
        eventsToAdd: List<Event> = listOf<Event>(),
        weightsToRemove: List<Weight> = listOf<Weight>(),
        photosToAdd: List<Photo> = listOf<Photo>(),
        photosToRemove: List<Photo> = listOf<Photo>(),
        petProfilePhotoToAdd: Photo? = null,
        petProfilePhotoToRemove: Photo? = null
    ) {
        val editedPet = Pet(
            petID = petID,
            petName = petName.value!!,
            petSpecies = petSpecies.value!!,
            petBreed = petBreed.value!!,
            petSex = petSex.value!!,
            petDOB = newPetDOB.selectedDate
        )
        viewModelScope.launch {
            petRepository.updatePet(
                pet=editedPet,
                eventsToAdd=eventsToAdd,
                eventsToRemove=eventsToRemove,
                weightsToRemove=weightsToRemove,
                photosToAdd = photosToAdd,
                photosToRemove = photosToRemove,
                petProfilePhotoToAdd = petProfilePhotoToAdd,
                petProfilePhotoToRemove = petProfilePhotoToRemove
            )
            _doneUpdating.value = true
        }
    }

    fun removeDOB() {
        newPetDOB.unSet()
    }

    // call after "yes" on "are you sure?"
    fun deletePet() {
        viewModelScope.launch {
            pet.value?.let {
                petRepository.deletePet(it)
            }
            _goToPetList.value = true
        }
    }

    fun wentBack() {
        _doneUpdating.value = false
    }

    fun wentToPetList() {
        _goToPetList.value = false
    }

    fun setPetSex(newSex: String) {
        petSex.value = newSex
    }

    companion object {
        fun provideFactory(petRepository: PetRepository, petID: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditPetViewModel::class.java)) {
                    return EditPetViewModel(petRepository, petID) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}