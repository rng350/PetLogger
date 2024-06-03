package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.selectiontracker.EditSelectionTracker
import kotlinx.coroutines.launch

class WeightMultiSelectionViewModel(weightRepository: WeightRepository, private val initialSelection: List<Weight> = listOf<Weight>()) : ViewModel() {
    private val _allWeights = MutableLiveData<List<CheckableItem<WeightWithPetName>>>()
    val allWeights get() = _allWeights
    val currentSelection = MutableLiveData<List<CheckableItem<WeightWithPetName>>>()
    val selectionTracker = EditSelectionTracker<WeightWithPetName>()
    private lateinit var fetchedAllWeights: List<WeightWithPetName>
    init {
        viewModelScope.launch {
            fetchedAllWeights = weightRepository.getAllWithPetNames()
            resetSelection()
        }
    }

    fun resetSelection() {
        val curSelectionTemp = mutableListOf<CheckableItem<WeightWithPetName>>()
        val selectionTrackerInitialList = mutableListOf<WeightWithPetName>()
        _allWeights.value = fetchedAllWeights.map {
            val inInitialSelection = initialSelection.contains(it.weight)
            val checkableItem = CheckableItem(it, MutableLiveData(inInitialSelection))
            if (inInitialSelection) {
                curSelectionTemp.add(checkableItem)
                selectionTrackerInitialList.add(it)
            }
            checkableItem
        }
        currentSelection.value = curSelectionTemp
        selectionTracker.initializeSelection(selectionTrackerInitialList.toList())
    }

    fun getWeightsToAdd(): List<Weight> {
        return selectionTracker.selectionToAdd.value?.map {
            it.item.weight
        } ?: listOf<Weight>()
    }

    fun getWeightsToRemove(): List<Weight> {
        return selectionTracker.selectionToRemove.value?.map {
            it.item.weight
        } ?: listOf<Weight>()
    }

    companion object {
        fun provideFactory(weightRepository: WeightRepository, initialSelection: List<Weight> = listOf<Weight>()): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WeightMultiSelectionViewModel::class.java)) {
                    return WeightMultiSelectionViewModel(weightRepository, initialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}