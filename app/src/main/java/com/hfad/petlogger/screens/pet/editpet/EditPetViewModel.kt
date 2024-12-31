package com.hfad.petlogger.screens.pet.editpet

import android.util.Log
import androidx.lifecycle.*
import com.hfad.petlogger.common.SelectableDateOptional
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.pets.usecases.GetPetDetailsForEditUseCase
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditPetViewModel(
    private val petId: Long,
    private val petRepository: PetRepository,
    private val getPetDetails: GetPetDetailsForEditUseCase
): ViewModel() {
    private val _initialPetName = MutableStateFlow<String>("")
    val initialPetName get() = _initialPetName
    val petName = MutableStateFlow<String>("")
    val petSpecies = MutableStateFlow<String>("")
    val petBreed = MutableStateFlow<String>("")

    val petSex = MutableStateFlow<String>("")
    val newPetDOB = SelectableDateOptional()
    val newPetDateOfPassing = SelectableDateOptional()
    private val _doneUpdating = MutableStateFlow(false)
    val doneUpdating: StateFlow<Boolean> get() = _doneUpdating
    private val _goToPetList = MutableStateFlow(false)
    val goToPetList: StateFlow<Boolean> get() = _goToPetList
    val petStatus = MutableStateFlow<PetStatus>(PetStatus.Active)

    init {
        viewModelScope.launch {
            val retrievedPet = getPetDetails()
            if (retrievedPet is GetPetDetailsForEditUseCase.Result.Success) {
                val petToEdit = retrievedPet.fetchedPet
                petName.value = petToEdit.petName
                _initialPetName.value = petToEdit.petName
                petSpecies.value = petToEdit.petSpecies
                petBreed.value = petToEdit.petBreed
                petSex.value = petToEdit.petSex
                petToEdit.petDateOfBirth?.let {
                    newPetDOB.set(it)
                }
                petToEdit.petDateOfPassing?.let {
                    newPetDateOfPassing.set(it)
                    petStatus.value = PetStatus.PassedAway
                }
            }
        }
    }

    fun updatePet(
        eventsToRemove: List<Long> = listOf<Long>(),
        eventsToAdd: List<Long> = listOf<Long>(),
        weightsToRemove: List<Long> = listOf<Long>(),
        photosToAdd: List<Photo> = listOf<Photo>(),
        photosToRemove: List<Photo> = listOf<Photo>(),
        petProfilePhotoToAdd: Photo? = null,
        petProfilePhotoToRemove: Photo? = null,
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>(),
        tagsToAdd: List<Tag> = listOf<Tag>(),
        tagsToRemove: List<Tag> = listOf<Tag>()
    ) {
        val editedPet = Pet(
            petID = petId,
            petName = petName.value,
            petSpecies = petSpecies.value,
            petBreed = petBreed.value,
            petSex = petSex.value,
            petDOB = newPetDOB.selectedDate
        )
        viewModelScope.launch {
            petRepository.updatePet(
                pet=editedPet,
                petDateOfPassing = if (petStatus.value is PetStatus.PassedAway) newPetDateOfPassing.selectedDate else null,
                eventsToAdd=eventsToAdd,
                eventsToRemove=eventsToRemove,
                weightsToRemove=weightsToRemove,
                photosToAdd = photosToAdd,
                photosToRemove = photosToRemove,
                petProfilePhotoToAdd = petProfilePhotoToAdd,
                petProfilePhotoToRemove = petProfilePhotoToRemove,
                notesToAdd = notesToAdd,
                notesToRemove = notesToRemove,
                notesToUpdate = notesToUpdate,
                tagsToAdd = tagsToAdd,
                tagsToRemove = tagsToRemove
            )
            _doneUpdating.value = true
        }
    }

    fun setPetStatus(newPetStatus: PetStatus) {
        petStatus.value = newPetStatus
    }

    fun removeDOB() {
        newPetDOB.unSet()
    }

    // call after "yes" on "are you sure?"
    fun deletePet() {
        viewModelScope.launch {
            petRepository.deletePet(Pet(petID = petId))
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

    sealed class Status {
        data object Loading: Status()
        data class Loaded(val result: GetPetDetailsForEditUseCase.Result): Status()
    }

    sealed class PetStatus {
        data object Active: PetStatus()
        data object PassedAway: PetStatus()
    }

    companion object {
        fun provideFactory(petId: Long, petRepository: PetRepository, getPetDetails: GetPetDetailsForEditUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditPetViewModel::class.java)) {
                    return EditPetViewModel(petId, petRepository, getPetDetails) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}