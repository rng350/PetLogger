package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.fetchers.FetchPetListForEditSelectionUseCase
import com.hfad.petlogger.photodisplay.stateless.GetAllCheckablePetsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetSingleItemUseCase
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.selectiontracker.SingleSelectionTracker
import kotlinx.coroutines.launch

class PetSingleSelectionViewModel(getAllCheckablePets: GetAllCheckablePetsUseCase, val initialPetSelectedId: Long? = null) : ViewModel() {
    val selectionTracker = SingleSelectionTracker<PetWithProfilePic>(
        allOptionsFetcher = getAllCheckablePets,
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
        fun provideFactory(getAllCheckablePets: GetAllCheckablePetsUseCase, initialPetSelectedId: Long? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetSingleSelectionViewModel::class.java)) {
                    return PetSingleSelectionViewModel(getAllCheckablePets, initialPetSelectedId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}