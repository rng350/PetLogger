package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker

class WeightMultiSelectionViewModel(
    getAllPets: GetItemsUseCase<WeightWithPetName>,
    getInitialSelection: GetItemsUseCase<WeightWithPetName>? = null
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<WeightWithPetName>(
        allOptionsFetcher = getAllPets,
        initialSelectionFetcher = getInitialSelection,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged

    fun resetSelection() {
        selectionTracker.resetSelection()
    }

    fun getWeightsToAdd(): List<Weight> {
        return selectionTracker.getSelectionToAdd().map{it.weight}
    }

    fun getWeightsToRemove(): List<Weight> {
        return selectionTracker.getSelectionToRemove().map{it.weight}
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
        fun provideFactory(
            getAllPets: GetItemsUseCase<WeightWithPetName>,
            getInitialSelection: GetItemsUseCase<WeightWithPetName>? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WeightMultiSelectionViewModel::class.java)) {
                    return WeightMultiSelectionViewModel(getAllPets, getInitialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}