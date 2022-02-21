package com.hfad.guineapiglog

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.OffsetDateTime
import kotlinx.coroutines.launch

class NewPetViewModel(val dao: PetDao) : ViewModel() {
    var petName : String = ""
    var petSpecies : String = ""
    var petBreed : String = ""
    var petSex : String = ""
    private var DOBInitialized : Boolean = false
    var petDOB : OffsetDateTime = OffsetDateTime.MIN
        set(givenDOB : OffsetDateTime) {
            petDOB = givenDOB
            DOBInitialized = true
        }

    fun addPet() {
        viewModelScope.launch {
            val pet = Pet()
            pet.petName = petName
            pet.petSpecies = petSpecies
            pet.petBreed = petBreed
            pet.petSex = petSex
            pet.petDOB = petDOB
            pet.hasDOB = DOBInitialized
        }
    }

    fun unsetDOB() {
        petDOB = OffsetDateTime.MIN
        DOBInitialized = false
    }

    fun showDatePickerDialog() {

    }
}