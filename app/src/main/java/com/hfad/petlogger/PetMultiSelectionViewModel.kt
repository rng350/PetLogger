package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.repositories.NoteRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker
import kotlinx.coroutines.launch

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

    fun getPetsToAdd(): List<Pet> {
        return selectionTracker.getSelectionToAdd().map{it.pet}
    }

    fun getPetsToRemove(): List<Pet> {
        return selectionTracker.getSelectionToRemove().map{it.pet}
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