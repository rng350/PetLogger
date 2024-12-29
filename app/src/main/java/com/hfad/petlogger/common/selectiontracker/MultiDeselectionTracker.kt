package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem

class MultiDeselectionTracker<T> {
    private val selectionToRemove = HashSet<T>()

    fun toggle(checkableItem: CheckableItem<T>) {
        if (selectionToRemove.contains(checkableItem.item)) {
            selectionToRemove.remove(checkableItem.item)
            checkableItem.isChecked.value = false
        } else {
            selectionToRemove.add(checkableItem.item)
            checkableItem.isChecked.value = true
        }
    }

    fun getSelectionToRemove(): List<T> = selectionToRemove.toList()

    fun resetSelection() {
        selectionToRemove.clear()
    }

    fun filterQueriedItemsForDisplay(itemsToFilter: List<T>, displayMode: Display): List<CheckableItem<T>> {
        return when (displayMode) {
            Display.All -> {
                itemsToFilter.map { CheckableItem(it, MutableLiveData(selectionToRemove.contains(it))) }
            }
            Display.None -> {
                listOf()
            }
            Display.SelectionToKeep -> {
                itemsToFilter.filterNot { selectionToRemove.contains(it) }.map { CheckableItem(it, MutableLiveData(selectionToRemove.contains(it))) }
            }
            Display.SelectionToRemove -> {
                itemsToFilter.filter { selectionToRemove.contains(it) }.map { CheckableItem(it, MutableLiveData(selectionToRemove.contains(it))) }
            }
        }
    }

    sealed class Display {
        data object All: Display()
        data object SelectionToKeep: Display()
        data object SelectionToRemove: Display()
        data object None: Display()
    }
}