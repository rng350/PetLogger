package com.hfad.petlogger.screens.pet.newpet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.SelectableDateOptional
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.pets.PetRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewPetViewModel(private val petRepository: PetRepository) : ViewModel() {
    var petName : String = ""
    var petSpecies : String = ""
    var petBreed : String = ""
    private var _petSex : String = ""
    val petSex: String
        get() = _petSex
    var petDOB : SelectableDateOptional = SelectableDateOptional()
    val goToViewPet = MutableLiveData<Long>()

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
                    petRepository.addPet(pet = pet, photos = petPhotos, profilePic = petProfilePhoto, notes=notes, tags=tags)
                }.await()
                goToViewPet.value = addedPetId
            }
        }
    }

    fun setPetSex(newPetSex: String) {
        _petSex = newPetSex
    }

    fun clear() {

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