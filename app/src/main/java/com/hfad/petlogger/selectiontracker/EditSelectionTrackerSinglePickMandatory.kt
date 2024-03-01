package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.mutableCopyOf

// for editing selections wherein you have to have exactly one thing pciked
class EditSelectionTrackerSinglePickMandatory<T: CheckableItem<U>, U>(): EditSelectionTrackerInterface<T,U> {
    var initialSelection: U? = null
    var initialSelectionCheckable: T? = null
    override val selectionToAdd = MutableLiveData(mutableListOf<T>())
    override val selectionToRemove = MutableLiveData(mutableListOf<T>())
    override val itemsSelectedAmt = MutableLiveData<Int>(0)
    override fun initializeSelection(selection: List<U>) {
        require(selection.size == 1)
        initialSelection = selection[0]
        itemsSelectedAmt.value = 1
    }

    fun initializeCheckableSelection(checkable: T) {
        initialSelectionCheckable = checkable
        checkable.isChecked.value = true
    }

    override fun toggle(checkable: T) {
        if (inInitialSelection(checkable.item)) {
            // re-pick initial selection...
            if (selectionToRemove.value!!.contains(checkable)) {
                removeNewSelection()
                val newList = selectionToRemove.value!!.mutableCopyOf()
                newList.remove(checkable)
                checkable.isChecked.value = true
                selectionToRemove.value = newList
                itemsSelectedAmt.value = 1
            }
        } else {
            // pick new selection...
            if (!selectionToAdd.value!!.contains(checkable)) {
                removeInitSelection()
                removeNewSelection()
                val newList = selectionToAdd.value!!.mutableCopyOf()
                newList.add(checkable)
                checkable.isChecked.value = true
                selectionToAdd.value = newList
                itemsSelectedAmt.value = 1
            }
        }
    }

    private fun removeInitSelection() {
        initialSelectionCheckable?.let {
            if (selectionToRemove.value!!.isEmpty()) {
                val newList = selectionToRemove.value!!.mutableCopyOf()
                newList.add(it)
                it.isChecked.value = false
                selectionToRemove.value = newList
            }
        }
    }

    private fun removeNewSelection() {
        if (selectionToAdd.value!!.isNotEmpty()) {
            val newList = selectionToAdd.value!!.mutableCopyOf()
            val removed = newList.removeAt(0)
            removed.isChecked.value = false
            selectionToAdd.value = newList
        }
    }

    override fun inInitialSelection(item: U): Boolean {
        return initialSelection?.equals(item) == true
    }

    override fun clear() {
        itemsSelectedAmt.value = itemsSelectedAmt.value!! - selectionToAdd.value!!.size
        selectionToAdd.value = mutableListOf<T>()
    }

    override fun canSelectMore(): Boolean {
        return true
    }



}