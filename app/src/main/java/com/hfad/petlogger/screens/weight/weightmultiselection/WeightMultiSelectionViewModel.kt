package com.hfad.petlogger.screens.weight.weightmultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.weights.WeightWithPetName
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker

class WeightMultiSelectionViewModel(
    getAllPets: GetItemsUseCase<WeightWithPetName>,
    getInitialSelection: GetItemsUseCase<WeightWithPetName>? = null,
    getInitialNewSelection: GetItemsUseCase<WeightWithPetName>? = null
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<WeightWithPetName>(
        allOptionsFetcher = getAllPets,
        initialSelectionFetcher = getInitialSelection,
        initialNewSelectionFetcher = getInitialNewSelection,
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