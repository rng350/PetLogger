package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SingleSelectionTracker<T>(
    private val allOptionsFetcher: GetItemsUseCase<CheckableItem<T>>,
    coroutineScope: CoroutineScope) {
    private var _initialSelection: CheckableItem<T>? = null
    // for dialog
    val allOptions = MutableLiveData<List<CheckableItem<T>>>()
    // for displaying
    private val _currentSelection = MutableLiveData<CheckableItem<T>>()
    val currentSelection: LiveData<CheckableItem<T>> get() = _currentSelection
    // in-between
    private val _prospectiveSelection = MutableLiveData<CheckableItem<T>>()
    val prospectiveSelection: LiveData<CheckableItem<T>> get() = _prospectiveSelection
    init {
        coroutineScope.launch {
            val allFetched = allOptionsFetcher()
            allOptions.postValue(allFetched)
            allOptions.value?.first { it.isChecked.value == true }.let {
                it?.let {
                    _initialSelection = it
                    resetSelection()
                }
            }
        }
    }

    // call when pressing "Cancel" in dialog
    fun cancelProspectiveSelection() {
        // uncheck prospective selection
        _prospectiveSelection.value?.let {
            it.isChecked.value = false
        }
        val currentSelectedItem = _currentSelection.value
        currentSelectedItem?.let {
            it.isChecked.value = true
            _prospectiveSelection.value = it
        }
    }

    // call when pressing "Ok" in dialog
    fun confirmProspectiveSelection() {
        _prospectiveSelection.value?.let {
            _currentSelection.value = it
        }
    }

    // call when pressing on any item in dialog
    fun toggle(newProspectiveSelection: CheckableItem<T>) {
        if (newProspectiveSelection.item != _prospectiveSelection) {
            // uncheck previous prospective selection
            _prospectiveSelection.value?.let {
                it.isChecked.value = false
            }

            newProspectiveSelection.isChecked.value = true
            _prospectiveSelection.value = newProspectiveSelection
        }
    }

    // call when submitting in display fragment
    fun getCurrentSelection(): T? {
        return currentSelection.value?.item
    }

    // call when resetting in display fragment
    fun resetSelection() {
        _initialSelection?.let { initSelection ->
            _currentSelection.value?.let {curSelection ->
                curSelection.isChecked.value = false
            }
            _prospectiveSelection.value?.let {prospSelection ->
                prospSelection.isChecked.value = false
            }
            _currentSelection.value = initSelection
            _prospectiveSelection.value = initSelection
            initSelection.isChecked.value = true
        }
    }
}