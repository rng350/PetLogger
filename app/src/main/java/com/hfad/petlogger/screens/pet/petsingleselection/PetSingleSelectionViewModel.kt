package com.hfad.petlogger.screens.pet.petsingleselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.common.selectiontracker.SingleSelectionTracker
import com.hfad.petlogger.common.usecases.GetSingleInitialItemUseCase
import com.hfad.petlogger.pets.usecases.GetAllPetsWithProfilePhotosUseCase

class PetSingleSelectionViewModel(getAllPets: GetAllPetsWithProfilePhotosUseCase, val initialPetSelected: GetSingleInitialItemUseCase<PetWithProfilePic>? = null) : ViewModel() {
    val selectionTracker = SingleSelectionTracker<PetWithProfilePic>(
        allOptionsFetcher = getAllPets,
        initialItemFetcher = initialPetSelected,
        coroutineScope =  viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged

    // assumes a selection is mandatory; will only revert to original selection if there's anything to revert to
    fun resetSelection() {
        selectionTracker.resetSelection()
    }

    fun cancelSelection() {
        selectionTracker.cancelProspectiveSelection()
    }

    fun confirmSelection() {
        selectionTracker.confirmProspectiveSelection()
        _currentSelectionChanged = true
    }

    fun onCurrentSelectionChanged() {
        _currentSelectionChanged = false
    }

    companion object {
        fun provideFactory(getAllPets: GetAllPetsWithProfilePhotosUseCase, initialPetSelected: GetSingleInitialItemUseCase<PetWithProfilePic>? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetSingleSelectionViewModel::class.java)) {
                    return PetSingleSelectionViewModel(getAllPets, initialPetSelected) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}