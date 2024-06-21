package com.hfad.petlogger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.PetWeightForDisplay
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.photodisplay.stateless.GetAssociatedItemsUseCase
import com.hfad.petlogger.selectiontracker.NewSelectionTracker
import kotlinx.coroutines.launch

class PetWeightDeselectionViewModel(private val getWeights: GetAssociatedItemsUseCase<CheckableItem<PetWeightForDisplay>>) : ViewModel() {
    private val _weights = MutableLiveData<List<CheckableItem<PetWeightForDisplay>>>()
    val weights: LiveData<List<CheckableItem<PetWeightForDisplay>>> get() = _weights
    val selectionTracker = NewSelectionTracker<PetWeightForDisplay>()
    init {
        viewModelScope.launch {
            _weights.value = getWeights() ?: listOf()
        }
    }

    fun getWeightsToRemove(): List<Weight> {
        val fromSelection = selectionTracker.selectionToAdd.value?.toList() ?: listOf()
        return fromSelection.map { it.item.weight }
    }
    fun reset() {
        selectionTracker.selectionToAdd.value?.forEach {
            selectionTracker.remove(it)
        }
    }

    companion object {
        fun provideFactory(getWeights: GetAssociatedItemsUseCase<CheckableItem<PetWeightForDisplay>>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetWeightDeselectionViewModel::class.java)) {
                    return PetWeightDeselectionViewModel(getWeights) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}