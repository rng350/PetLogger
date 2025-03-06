package com.hfad.petlogger.screens.pet.newpet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.datetimeselection.SelectableDateOptional
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.pets.data.Pet
import com.hfad.petlogger.pets.domain.PetRepository
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.tags.data.Tag
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewPetViewModel(private val petRepository: PetRepository) : ViewModel() {
    var petName : String = ""
    var petSpecies : String = ""
    var petBreed : String = ""
    private var _petSex : String = ""
    val petSex: String
        get() = _petSex
    val petDOB : SelectableDateOptional = SelectableDateOptional()
    val petDateOfPassing : SelectableDateOptional = SelectableDateOptional()
    val goToViewPet = MutableLiveData<Long>()
    private val _petStatus = MutableStateFlow<PetStatus>(PetStatus.Active)
    val petStatus: StateFlow<PetStatus> get() = _petStatus

    fun reset() {
        petName = ""
        petSpecies = ""
        petBreed = ""
        _petSex = ""
    }

    fun addPet(
        petProfilePhoto: Photo? = null,
        petPhotos: List<Photo> = listOf<Photo>(),
        events: List<Long> = listOf<Long>(),
        notes: List<Note> = listOf<Note>(),
        tags: List<Tag> = listOf<Tag>()
    ) {
        if (petName.isNotEmpty()) {
            viewModelScope.launch {
                val pet = Pet()
                pet.petName = petName
                pet.petSpecies = petSpecies
                pet.petBreed = petBreed
                pet.petSex = _petSex
                pet.petDOB = petDOB.selectedDate
                val addedPetId = async {
                    petRepository.addPet(
                        pet = pet,
                        petDateOfPassing = if (petStatus.value is PetStatus.PassedAway) petDateOfPassing.selectedDate else null,
                        photos = petPhotos,
                        profilePic = petProfilePhoto,
                        notes=notes,
                        tags=tags
                    )
                }.await()
                goToViewPet.value = addedPetId
            }
        }
    }

    fun setPetSex(newPetSex: String) {
        _petSex = newPetSex
    }

    fun setPetStatus(newPetStatus: PetStatus) {
        _petStatus.value = newPetStatus
    }

    fun clear() {

    }

    sealed class PetStatus {
        data object Active: PetStatus()
        data object PassedAway: PetStatus()
    }

    companion object {
        fun provideFactory(petRepository: PetRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NewPetViewModel::class.java)) {
                    return NewPetViewModel(petRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}