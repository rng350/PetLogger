package com.hfad.guineapiglog

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.OffsetDateTime
import kotlinx.coroutines.launch

class NewPetViewModel(val dao: PetDao) : ViewModel() {
    var petName : String = ""
    var petSpecies : String = ""
    var petBreed : String = ""
    var petSex : String = ""
    var petProfilePicURI : Uri? = null
    var petDOB : SelectableDateOptional = SelectableDateOptional()

    fun addPet() {
        Log.i("PET_ADDING", "trying to add pet... name:${petName}")
        if (petName.isNotEmpty()) {
            viewModelScope.launch {
                val pet = Pet(petProfilePic = petProfilePicURI)
                pet.petName = petName
                pet.petSpecies = petSpecies
                pet.petBreed = petBreed
                pet.petSex = petSex
                pet.petDOB = petDOB.dateTime
                pet.hasDOB = petDOB.hasBeenSet
                Log.i("PET_ADDING", "trying to add pet... name: ${pet.petName}, id: ${pet.petID},hasDob: ${pet.hasDOB}, dob: ${pet.petDOB.toString()}")
                dao.insert(pet)
                Log.i("PET_ADDING", "Pet has been ADDED!")
            }
        }
    }
}