package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import kotlinx.coroutines.launch

// should be included in main fragment too
class PetMultiSelectionViewModel(private val petRepository: PetRepository, private val initialSelection: List<Pet> = listOf<Pet>()) : ViewModel() {
    // for all selection options in dialog
    val allPets = MutableLiveData<List<CheckableItem<PetWithProfilePic>>>()
    // for selection display
    val currentSelection = MutableLiveData<List<CheckableItem<PetWithProfilePic>>>()
    // for adding/removing from database
    val selectionTracker = EditSelectionTracker<PetWithProfilePic>()

    private lateinit var fetchedAllPets: List<PetWithProfilePic>
    init {
        viewModelScope.launch {
            fetchedAllPets = petRepository.getAllPets()
            Log.d("PetMSelect_VM", "fetchedPets: ${fetchedAllPets}")
            resetSelection()
        }
    }

    fun logSomething(tag: String = "", message: String = "") {
        Log.d(tag, message)
    }

    fun resetSelection() {
        val currentSelectionTemp = mutableListOf<CheckableItem<PetWithProfilePic>>()
        val selectionTrackerInitialList = mutableListOf<PetWithProfilePic>()
        allPets.value = fetchedAllPets.map {
            if (initialSelection.contains(it.pet)) {
                val checkablePet = CheckableItem(it, isChecked = MutableLiveData(true))
                currentSelectionTemp.add(checkablePet)
                selectionTrackerInitialList.add(it)
                checkablePet
            } else CheckableItem(it)
        }
        currentSelection.value = currentSelectionTemp.toList()
        selectionTracker.initializeSelection(selectionTrackerInitialList.toList())
        Log.d("PetMSelect_VM", "allPets: ${allPets.value}")
        Log.d("PetMSelect_VM", "currentselection: ${currentSelection.value}")
        Log.d("PetMSelect_VM", "selectionTracker init list: ${selectionTrackerInitialList}")
    }

    fun getPetsToAdd(): List<Pet> {
        val petsToAdd = selectionTracker.selectionToAdd.value?.map {
            it.item.pet
        } ?: listOf<Pet>()
        return petsToAdd
    }

    fun getPetsToRemove(): List<Pet> {
        val petsToRemove = selectionTracker.selectionToRemove.value?.map {
            it.item.pet
        } ?: listOf<Pet>()
        return petsToRemove
    }

    override fun onCleared() {
        super.onCleared()
        // destroy all observers
    }

    companion object {
        fun provideFactory(petRepository: PetRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetMultiSelectionViewModel::class.java)) {
                    return PetMultiSelectionViewModel(petRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}