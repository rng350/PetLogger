package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem

class EditSelectionTrackerSinglePick<T: CheckableItem<U>, U>(): EditSelectionTrackerInterface<T,U> {
    var initialSelection: U? = null
    var initialSelectionCheckable: T? = null
    override val selectionToAdd = MutableLiveData(mutableListOf<T>())
    override val selectionToRemove = MutableLiveData(mutableListOf<T>())
    override val itemsSelectedAmt = MutableLiveData<Int>(0)

    override fun initializeSelection(selection: List<U>) {
        if (selection.isNotEmpty()) {
            initialSelection = selection[0]
            itemsSelectedAmt.value = 1
        }
    }

    fun initializeCheckableSelection(checkable: T) {
        initialSelectionCheckable = checkable
        checkable.isChecked.value = true
    }

    override fun toggle(checkable: T) {
        if (inInitialSelection(checkable.item)) {
            if (selectionToRemove.value!!.contains(checkable)) {
                removeNewSelection()
                selectionToRemove.value!!.remove(checkable)
                checkable.isChecked.value = true
                itemsSelectedAmt.value = 1
            } else {
                removeInitSelection()
                itemsSelectedAmt.value = 0
            }
        } else {
            if (selectionToAdd.value!!.contains(checkable)) {
                removeNewSelection()
                itemsSelectedAmt.value = 0
            } else {
                removeInitSelection()
                removeNewSelection()
                selectionToAdd.value!!.add(checkable)
                checkable.isChecked.value = true
                itemsSelectedAmt.value = 1
            }
        }
    }

    private fun removeInitSelection() {
        initialSelectionCheckable?.let {
            if (selectionToRemove.value!!.isEmpty()) {
                selectionToRemove.value!!.add(it)
                it.isChecked.value = false
            }
        }
    }

    private fun removeNewSelection() {
        if (selectionToAdd.value!!.isNotEmpty()) {
            val removed = selectionToAdd.value!!.removeAt(0)
            removed.isChecked.value = false
        }
    }

    override fun inInitialSelection(item: U): Boolean {
        return initialSelection?.equals(item) == true
    }

    override fun clear() {
        itemsSelectedAmt.value = itemsSelectedAmt.value!!.minus(selectionToAdd.value!!.size)
        selectionToAdd.value!!.clear()
    }

    override fun canSelectMore(): Boolean {
        return true
    }



}