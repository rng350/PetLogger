package com.hfad.petlogger.screens.weight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.selectiontracker.MultiDeselectionDisplay
import com.hfad.petlogger.weights.PetWeightForSelection
import kotlinx.coroutines.flow.StateFlow

class PetWeightDeselectionViewModel(val deselectionTrackerDisplay: MultiDeselectionDisplay<PetWeightForSelection>) : ViewModel() {
    val weightDisplay: StateFlow<List<CheckableItem<PetWeightForSelection>>> get() = deselectionTrackerDisplay.currentDisplayedItems
    private val _toKeepButtonChecked = MutableLiveData(true)
    val toKeepButtonChecked: LiveData<Boolean> get() = _toKeepButtonChecked
    private val _toRemoveButtonChecked = MutableLiveData(true)
    val toRemoveButtonChecked: LiveData<Boolean> get() = _toRemoveButtonChecked

    fun onQueryTextSubmit(query: String?) {
        query?.let {
            deselectionTrackerDisplay.newQuery(query)
        }
    }

    fun onQueryTextChanged(newText: String?) {
        newText?.let {
            deselectionTrackerDisplay.newQuery(newText)
        }
    }

    fun getWeightsToRemove(): List<Long> {
        return deselectionTrackerDisplay.getSelectionToRemove().map {it.weightId}
    }

    fun reset() {
        deselectionTrackerDisplay.resetSelection()
    }

    fun isLoading(): Boolean {
        return deselectionTrackerDisplay.isLoading()
    }

    fun onLastPage(): Boolean {
        return deselectionTrackerDisplay.isLastPage()
    }

    fun loadMore() {
        deselectionTrackerDisplay.loadMoreItems()
    }

    fun toggleToKeepButton() {
        _toKeepButtonChecked.value?.let {
            _toKeepButtonChecked.value = !it
        }
    }

    fun toggleToRemoveButton() {
        _toRemoveButtonChecked.value?.let {
            _toRemoveButtonChecked.value = !it
        }
    }

    companion object {
        fun provideFactory(deselectionTrackerDisplay: MultiDeselectionDisplay<PetWeightForSelection>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetWeightDeselectionViewModel::class.java)) {
                    return PetWeightDeselectionViewModel(deselectionTrackerDisplay) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}