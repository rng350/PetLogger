package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem

class MediaMultiSelectionTracker<T>(
    private val checkItemIsInToAddList: CheckItemIsInSelectionUseCase<T>,
    private val checkItemIsInToRemoveList: CheckItemIsInSelectionUseCase<T>,
    private val checkItemIsInToKeepList: CheckItemIsInSelectionUseCase<T>
) {
    private var initialSelectionToAdd: List<CheckableItem<T>> = listOf()
    private var initialSelectionToKeep: List<CheckableItem<T>> = listOf()
    private var _currentSelectionCount = 0
    val currentSelectionCount: Int get() = _currentSelectionCount

    fun initializeSelectionToKeep(itemsList: List<T>) {
        initialSelectionToKeep = itemsList.map { CheckableItem(it, MutableLiveData(false)) }
        checkItemIsInToKeepList.addCheckableItems(initialSelectionToKeep)
        _currentSelectionCount = initialSelectionToKeep.size
    }

    fun initializeSelectionToAdd(item: T) {
        initialSelectionToAdd = listOf(CheckableItem(item, MutableLiveData(false)))
        checkItemIsInToAddList.addCheckableItems(initialSelectionToAdd)
        _currentSelectionCount = initialSelectionToAdd.size
    }

    fun toggle(checkableItem: CheckableItem<T>) {
        // 1. Check what's got to be done...
        // if in ToAdd, remove from it
        if (checkItemIsInToAddList.containsCheckableItem(checkableItem) != null) {
            checkItemIsInToAddList.removeCheckableItem(checkableItem)
            _currentSelectionCount--
        }
        // if in ToRemove, remove from it & add to ToKeep
        else if (checkItemIsInToRemoveList.containsCheckableItem(checkableItem) != null) {
            val restoredItem = checkItemIsInToRemoveList.removeCheckableItem(checkableItem)
            restoredItem?.let {
                checkItemIsInToKeepList.addCheckableItem(restoredItem)
            }
            _currentSelectionCount++
        }
        // otherwise, it was in the ToKeep selection, so add to ToRemove
        else {
            val removedItem = checkItemIsInToKeepList.removeCheckableItem(checkableItem)
            removedItem?.let {
                checkItemIsInToRemoveList.addCheckableItem(removedItem)
            }
            _currentSelectionCount--
        }
        // 2. Reload current selection
    }

    // returns whether or not any change to the current selection has been made
    fun addNewItems(items: List<T>): Boolean {
        // to prevent unnecessary observer notifications and refreshing
        var selectionToAddHasChanged = false
        var selectionToRemoveHasChanged = false

        for (item in items) {
            if (checkItemIsInToRemoveList.containsItem(item) != null) {
                val restoredItem = checkItemIsInToRemoveList.removeItem(item)
                restoredItem?.let {
                    checkItemIsInToKeepList.addCheckableItem(restoredItem)
                    selectionToRemoveHasChanged = true
                    _currentSelectionCount++
                }
            }
            else if (checkItemIsInToAddList.containsItem(item)==null && checkItemIsInToKeepList.containsItem(item)==null) {
                checkItemIsInToAddList.addItem(item)
                selectionToAddHasChanged = true
                _currentSelectionCount++
            }
        }
        return selectionToAddHasChanged || selectionToRemoveHasChanged
    }

    fun filterQueriedItemsForDisplay(itemsToFilter: List<T>, displayMode: Display): List<CheckableItem<T>> {
        return when (displayMode) {
            Display.All -> {
                checkItemIsInToAddList.getList() + itemsToFilter.mapNotNull { checkItemIsInToKeepList.containsItem(it) ?: checkItemIsInToRemoveList.containsItem(it) }
            }
            Display.None -> {
                listOf()
            }
            Display.SelectionToAdd -> {
                checkItemIsInToAddList.getList()
            }
            Display.SelectionToAddAndKeep -> {
                checkItemIsInToAddList.getList() + itemsToFilter.mapNotNull { checkItemIsInToKeepList.containsItem(it) }
            }
            Display.SelectionToAddAndRemove -> {
                checkItemIsInToAddList.getList() + itemsToFilter.mapNotNull { checkItemIsInToRemoveList.containsItem(it) }
            }
            Display.SelectionToKeep -> {
                itemsToFilter.mapNotNull { checkItemIsInToKeepList.containsItem(it) }
            }
            Display.SelectionToKeepAndRemove -> {
                itemsToFilter.mapNotNull { checkItemIsInToKeepList.containsItem(it) ?: checkItemIsInToRemoveList.containsItem(it) }
            }
            Display.SelectionToRemove -> {
                itemsToFilter.mapNotNull { checkItemIsInToRemoveList.containsItem(it) }
            }
        }
    }

    fun getSelectionToAdd(): List<T> = checkItemIsInToAddList.getList().map{it.item}
    fun getSelectionToRemove(): List<T> = checkItemIsInToRemoveList.getList().map{it.item}

    fun resetSelection() {
        checkItemIsInToAddList.resetToCheckableItemList(initialSelectionToAdd)
        checkItemIsInToKeepList.resetToCheckableItemList(initialSelectionToKeep)
        checkItemIsInToRemoveList.resetToCheckableItemList(listOf<CheckableItem<T>>())
        _currentSelectionCount = if (initialSelectionToKeep.isNotEmpty()) initialSelectionToKeep.size else initialSelectionToAdd.size
    }

    sealed class Display {
        data object All: Display()
        data object SelectionToAddAndKeep: Display()
        data object SelectionToKeepAndRemove: Display()
        data object SelectionToAddAndRemove: Display()
        data object SelectionToAdd: Display()
        data object SelectionToKeep: Display()
        data object SelectionToRemove: Display()
        data object None: Display()
    }
}