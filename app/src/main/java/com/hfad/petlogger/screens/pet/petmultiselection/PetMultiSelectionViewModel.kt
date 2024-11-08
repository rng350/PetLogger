package com.hfad.petlogger.screens.pet.petmultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.common.usecases.GetItemsUseCase

// should be included in main fragment too
class PetMultiSelectionViewModel(
    getAllPets: GetItemsUseCase<PetWithProfilePic>,
    getInitialSelection: GetItemsUseCase<PetWithProfilePic>? = null
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<PetWithProfilePic>(
        allOptionsFetcher = getAllPets,
        initialSelectionFetcher = getInitialSelection,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged

    fun getPetsToAdd(): List<Long> {
        return selectionTracker.getSelectionToAdd().map{it.petId}
    }

    fun getPetsToRemove(): List<Long> {
        return selectionTracker.getSelectionToRemove().map{it.petId}
    }

    fun confirmSelection() {
        selectionTracker.confirmProspectiveSelection()
        _currentSelectionChanged = true
    }

    fun onCurrentSelectionChanged() {
        _currentSelectionChanged = false
    }

    fun reset() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
    }

    companion object {
        fun provideFactory(getAllPets: GetItemsUseCase<PetWithProfilePic>, getInitialSelection: GetItemsUseCase<PetWithProfilePic>? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetMultiSelectionViewModel::class.java)) {
                    return PetMultiSelectionViewModel(getAllPets, getInitialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}