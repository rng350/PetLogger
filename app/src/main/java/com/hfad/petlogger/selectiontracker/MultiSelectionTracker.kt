package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.copyOf
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
INITIAL SELECTION
Pre-existing selection, prior to creation of display & dialog fragments.

CURRENT SELECTION
Items marked for becoming the new selection. Already confirmed from dialog selection. Changes not yet submitted to database.

PROSPECTIVE SELECTION
Items marked for selection in dialog, not yet confirmed
 **/
class MultiSelectionTracker<T>(
    allOptionsFetcher: GetItemsUseCase<T>,
    initialSelectionFetcher: GetItemsUseCase<T>? = null,
    coroutineScope: CoroutineScope,
    private val choiceLimit: Int = Int.MAX_VALUE
) {
    private val initialSelection = HashSet<T>()
    // for dialog
    private val _allOptions = MutableLiveData<List<CheckableItem<T>>>()
    val allOptions: LiveData<List<CheckableItem<T>>> get() = _allOptions
    // for displaying
    private val _currentSelection = MutableLiveData<List<CheckableItem<T>>>()
    val currentSelection: LiveData<List<CheckableItem<T>>> get() = _currentSelection
    // in-between
    private val _prospectiveSelection = MutableLiveData<List<CheckableItem<T>>>()
    val prospectiveSelection: LiveData<List<CheckableItem<T>>> get() = _prospectiveSelection

    init {
        coroutineScope.launch {
            val allOptionsDeferred = async {
                allOptionsFetcher()
            }
            initialSelectionFetcher?.let {
                val initialPicksDeferred = async {
                    initialSelectionFetcher()
                }
                val initialPicks = initialPicksDeferred.await()
                initialSelection.addAll(initialPicks)
            }
            val currentSelectionTemp = mutableListOf<CheckableItem<T>>()
            _allOptions.value = allOptionsDeferred.await().map {
                if (initialSelection.contains(it)) {
                    val checkableItem = CheckableItem(it, MutableLiveData(true))
                    currentSelectionTemp.add(checkableItem)
                    checkableItem
                } else {
                    CheckableItem(it, MutableLiveData(false))
                }
            }
            _currentSelection.value = currentSelectionTemp
            _prospectiveSelection.value = currentSelectionTemp
        }
    }

    // call when pressing "Cancel" in dialog
    fun cancelProspectiveSelection() {
        // set prospective to current
        currentSelection.value?.let { currentSelectionList ->
            currentSelectionList.onEach { it -> it.isChecked.value = true }
            prospectiveSelection.value?.let { prospectiveSelectionList ->
                prospectiveSelectionList.onEach { it -> it.isChecked.value = currentSelectionList.contains(it) }
            }
            _prospectiveSelection.value = currentSelection.value
        }
    }

    // call when pressing "Ok" in dialog
    fun confirmProspectiveSelection() {
        // set current to prospective
        _currentSelection.value = prospectiveSelection.value
    }

    // call when pressing on any item in dialog
    fun toggle(checkableItem: CheckableItem<T>) {
        _prospectiveSelection.value?.let {
            // if in prospective, remove from it
            if (it.contains(checkableItem)) {
                checkableItem.isChecked.value = false
                val listCopy = it.toMutableList()
                listCopy.remove(checkableItem)
                _prospectiveSelection.value = listCopy
            } else {
                // if not in prospective, add to it
                if (prospectiveSelection.value!=null && prospectiveSelection.value!!.size<choiceLimit) {
                    checkableItem.isChecked.value = true
                    val listCopy = it.toMutableList()
                    listCopy.add(checkableItem)
                    _prospectiveSelection.value = listCopy
                }
            }
        }
    }

    // call when submitting in display fragment
    fun getSelectionToAdd(): List<T> {
        // anything in current that's not in initial selection
        val selectionTemp = mutableListOf<T>()
        currentSelection.value?.let { currentSelectionList ->
            selectionTemp.addAll(currentSelectionList.map{it.item}.filterNot{initialSelection.contains(it)})
        }
        return selectionTemp.toList()
    }

    // call when submitting in display fragment
    fun getSelectionToRemove(): List<T> {
        // anything in initial that's not in current
        val selectionTemp = mutableListOf<T>()
        val initialSelectionAsList = initialSelection.toList()
        currentSelection.value?.let { currentSelectionList ->
            val currentSelectionHash = currentSelectionList.map{it.item}.toHashSet()
            selectionTemp.addAll(initialSelectionAsList.filterNot { currentSelectionHash.contains(it) })
        }
        return selectionTemp
    }

    // call when clicking on item from display fragment
    fun remove(item: CheckableItem<T>) {
        // remove from both current & prospective selection lists
        item.isChecked.value = false
        _prospectiveSelection.value?.let {
            val listCopy = it.toMutableList()
            listCopy.remove(item)
            _prospectiveSelection.value = listCopy
        }
        _currentSelection.value?.let {
            val listCopy = it.toMutableList()
            listCopy.remove(item)
            _currentSelection.value = listCopy
        }
    }

    // call when resetting in display fragment
    fun resetSelection() {
        val currentSelectionTemp = mutableListOf<CheckableItem<T>>()

        _allOptions.value = allOptions.value?.onEach {
            if (initialSelection.contains(it.item)) {
                it.isChecked.value = true
                currentSelectionTemp.add(it)
            } else {
                it.isChecked.value = false
            }
        }?.copyOf() ?: listOf()

        _currentSelection.value = currentSelectionTemp
        _prospectiveSelection.value = currentSelectionTemp
    }
}