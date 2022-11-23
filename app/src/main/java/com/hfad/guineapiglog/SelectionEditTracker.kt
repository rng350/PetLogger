package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

/*
    1. fetch pets from viewmodel
    2. observer on petpetwithphoto, set to call
    -> 1. initialize selectionedittracker
    -> 2. initialize checkableitem petwithphoto, use shouldbechecked function from
    use that last checkable petwithphoto list for recycler view
*/
class SelectionEditTracker<T>(choiceLimitSet: Int?) {
    val initialSelection = hashSetOf<T>()
    private val selectionToAdd = MutableLiveData(mutableListOf<T>())
    private val selectionToRemove = MutableLiveData(mutableListOf<T>())
    val choiceLimit = MutableLiveData<Int>()
    val itemsSelectedAmt = MutableLiveData<Int>()

    init {
        choiceLimit.value = choiceLimitSet
    }

    fun initializeSelection(selection: List<T>) {
        for (item in selection) {
            initialSelection.add(item)
        }
        itemsSelectedAmt.value = selection.size
    }

    fun toggle(item: CheckableItem<T>) {
        item.item.let {
            val choiceLimit = choiceLimit.value ?: Int.MAX_VALUE
            if (initialSelection.contains(it)) {
                if (selectionToRemove.value!!.contains(it)) {
                    if (itemsSelectedAmt.value!! < choiceLimit) {
                        selectionToRemove.value!!.remove(it)
                        selectionToRemove.value = selectionToRemove.value
                        item.isChecked = true
                        itemsSelectedAmt.value = itemsSelectedAmt.value!! + 1
                    }
                } else {
                    selectionToRemove.value!!.add(it)
                    selectionToRemove.value = selectionToRemove.value
                    item.isChecked = false
                    itemsSelectedAmt.value = itemsSelectedAmt.value!! - 1
                }
            } else {
                if (selectionToAdd.value!!.contains(it)) {
                    selectionToAdd.value!!.remove(it)
                    selectionToAdd.value = selectionToAdd.value
                    item.isChecked = false
                    itemsSelectedAmt.value = itemsSelectedAmt.value!! - 1
                } else {
                    if (itemsSelectedAmt.value!! < choiceLimit) {
                        selectionToAdd.value!!.add(it)
                        selectionToAdd.value = selectionToAdd.value
                        item.isChecked = true
                        itemsSelectedAmt.value = itemsSelectedAmt.value!! + 1
                    }
                }
            }
        }
    }

    fun inInitialSelection(item: T): Boolean {
        return initialSelection.contains(item)
    }

    // helper function for recyclerview binding interface
    // call in viewmodel when initializing so as to properly set checks in the first place
    // should probably call in binding interface creation function too
    fun shouldBeChecked(item: T): Boolean {
        if (initialSelection.contains(item)) {
            return !(selectionToRemove.value!!.contains(item))
        } else return selectionToAdd.value!!.contains(item)
    }
}