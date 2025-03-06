package com.hfad.petlogger.screens.weight.weightmultiselection

import androidx.lifecycle.ViewModel
import com.hfad.petlogger.weights.data.WeightWithPetName
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase

class WeightMultiSelectionViewModel(
    getAllWeights: GetItemsUseCase<WeightWithPetName>,
    getInitialSelection: GetMultipleInitialItemsUseCase<WeightWithPetName>? = null,
) : ViewModel() {
    /*val selectionTracker = MultiSelectionTracker<WeightWithPetName>(
        allOptionsFetcher = getAllWeights,
        getInitialSelection = getInitialSelection,
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
            getInitialSelection: GetMultipleInitialItemsUseCase<WeightWithPetName>? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WeightMultiSelectionViewModel::class.java)) {
                    return WeightMultiSelectionViewModel(getAllPets, getInitialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }*/
}