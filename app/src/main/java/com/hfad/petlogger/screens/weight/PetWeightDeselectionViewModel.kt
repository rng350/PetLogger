package com.hfad.petlogger.screens.weight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.weights.PetWeightForSelection
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.selectiontracker.NewSelectionTracker
import kotlinx.coroutines.launch

class PetWeightDeselectionViewModel(private val getWeights: GetItemsUseCase<CheckableItem<PetWeightForSelection>>) : ViewModel() {
    private val _weights = MutableLiveData<List<CheckableItem<PetWeightForSelection>>>()
    val weights: LiveData<List<CheckableItem<PetWeightForSelection>>> get() = _weights
    val selectionTracker = NewSelectionTracker<PetWeightForSelection>()
    init {
        viewModelScope.launch {
            _weights.value = getWeights() ?: listOf()
        }
    }

    fun getWeightsToRemove(): List<Long> {
        val fromSelection = selectionTracker.selectionToAdd.value?.toList() ?: listOf()
        return fromSelection.map { it.item.weightId }
    }
    fun reset() {
        selectionTracker.selectionToAdd.value?.forEach {
            selectionTracker.remove(it)
        }
    }

    companion object {
        fun provideFactory(getWeights: GetItemsUseCase<CheckableItem<PetWeightForSelection>>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetWeightDeselectionViewModel::class.java)) {
                    return PetWeightDeselectionViewModel(getWeights) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}