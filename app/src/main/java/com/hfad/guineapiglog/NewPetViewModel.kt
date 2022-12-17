package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.selectiontracker.NewSelectionTracker
import com.hfad.guineapiglog.selectiontracker.SharedCounterSelectionTracker
import com.hfad.guineapiglog.selectiontracker.VariableSelectionMode
import kotlinx.coroutines.launch

class NewPetViewModel(val dao: PetDao) : ViewModel() {
    var petName : String = ""
    var petSpecies : String = ""
    var petBreed : String = ""
    private var _petSex : String = ""
    val petSex: String
        get() = _petSex
    var petDOB : SelectableDateOptional = SelectableDateOptional()
    val petID = MutableLiveData<Long>()
    val petPhotoSelection = NewSelectionTracker<Photo>(1)

    val tempSharedCounter = MutableLiveData<Int>(0)
    val tempDELETEMEBITCH = SharedCounterSelectionTracker<Photo>(1, tempSharedCounter, VariableSelectionMode.CUMULATIVE)

    fun addPet() {
        Log.i("PET_ADDING", "trying to add pet... name:${petName}")
        if (petName.isNotEmpty()) {
            viewModelScope.launch {
                val pet = Pet()
                pet.petName = petName
                pet.petSpecies = petSpecies
                pet.petBreed = petBreed
                pet.petSex = _petSex
                pet.petDOB = petDOB.dateTime
                pet.hasDOB = petDOB.hasBeenSet
                Log.i("PET_ADDING", "trying to add pet... name: ${pet.petName}, id: ${pet.petID},hasDob: ${pet.hasDOB}, dob: ${pet.petDOB.toString()}")
                petID.value = dao.insert(pet)
                Log.i("PET_ADDING", "Pet has been ADDED!")
            }
        }
    }

    fun setPetSex(newPetSex: String) {
        _petSex = newPetSex
    }
}