package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem

/*
    1. fetch pets from viewmodel
    2. observer on petpetwithphoto, set to call
    -> 1. initialize selectionedittracker
    -> 2. initialize checkableitem petwithphoto, use shouldbechecked function from
    use that last checkable petwithphoto list for recycler view
*/

class EditSelectionTrackerMultiPick<T: CheckableItem<U>, U>(choiceLimitSet: Int?): EditSelectionTrackerInterface<T,U> {
    private var initialSelection = hashSetOf<U>()
    override val selectionToAdd = MutableLiveData(mutableListOf<T>())
    override val selectionToRemove = MutableLiveData(mutableListOf<T>())
    val choiceLimit = MutableLiveData<Int>()
    override val itemsSelectedAmt = MutableLiveData<Int>()

    init {
        choiceLimit.value = choiceLimitSet
    }

    override fun initializeSelection(selection: List<U>) {
        initialSelection = selection.toHashSet()
        itemsSelectedAmt.value = selection.size
    }

    override fun toggle(checkable: T) {
        checkable.item.let {
            val choiceLimit = choiceLimit.value ?: Int.MAX_VALUE
            if (inInitialSelection(it)) {
                if (selectionToRemove.value!!.contains(checkable)) {
                    if (itemsSelectedAmt.value!! < choiceLimit) {
                        selectionToRemove.value!!.remove(checkable)
                        selectionToRemove.value = selectionToRemove.value
                        checkable.isChecked.value = true
                        itemsSelectedAmt.value = itemsSelectedAmt.value!! + 1
                    }
                } else {
                    selectionToRemove.value!!.add(checkable)
                    selectionToRemove.value = selectionToRemove.value
                    checkable.isChecked.value = false
                    itemsSelectedAmt.value = itemsSelectedAmt.value!! - 1
                }
            } else {
                if (selectionToAdd.value!!.contains(checkable)) {
                    selectionToAdd.value!!.remove(checkable)
                    selectionToAdd.value = selectionToAdd.value
                    checkable.isChecked.value = false
                    itemsSelectedAmt.value = itemsSelectedAmt.value!! - 1
                } else {
                    if (itemsSelectedAmt.value!! < choiceLimit) {
                        selectionToAdd.value!!.add(checkable)
                        selectionToAdd.value = selectionToAdd.value
                        checkable.isChecked.value = true
                        itemsSelectedAmt.value = itemsSelectedAmt.value!! + 1
                    }
                }
            }
        }
    }

    override fun inInitialSelection(item: U): Boolean {
        return initialSelection.contains(item)
    }

    override fun clear() {
        for (checkable in requireNotNull(selectionToAdd.value)) {
            checkable.isChecked.value = false
        }
        selectionToAdd.value!!.clear()
    }

    override fun canSelectMore(): Boolean {
        return ((initialSelection.size
                + selectionToAdd.value!!.size
                - selectionToRemove.value!!.size)

                < (choiceLimit.value ?: Int.MAX_VALUE))
    }
}