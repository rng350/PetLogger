package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.mutableCopyOf

/*
    1. fetch pets from viewmodel
    2. observer on petpetwithphoto, set to call
    -> 1. initialize selectionedittracker
    -> 2. initialize checkableitem petwithphoto, use shouldbechecked function from
    use that last checkable petwithphoto list for recycler view
*/

class EditSelectionTrackerMultiPick<T: CheckableItem<U>, U>(choiceLimitSet: Int? = null): EditSelectionTrackerInterface<T,U> {
    private var initialSelection = hashSetOf<U>()
    override val selectionToAdd = MutableLiveData(mutableListOf<T>())
    override val selectionToRemove = MutableLiveData(mutableListOf<T>())
    val choiceLimit = MutableLiveData<Int>()
    override val itemsSelectedAmt = MutableLiveData<Int>()

    init {
        choiceLimit.value = choiceLimitSet ?: Int.MAX_VALUE
    }

    override fun initializeSelection(selection: List<U>) {
        initialSelection = selection.toHashSet()
        itemsSelectedAmt.value = selection.size
        selectionToAdd.value = mutableListOf<T>()
        selectionToRemove.value = mutableListOf<T>()
    }

    override fun toggle(checkable: T) {
        checkable.item.let {
            val choiceLimit = choiceLimit.value ?: Int.MAX_VALUE
            if (inInitialSelection(it)) {
                val newToRemoveList = selectionToRemove.value!!.mutableCopyOf()
                if (selectionToRemove.value!!.contains(checkable)) {
                    if (itemsSelectedAmt.value!! < choiceLimit) {
                        newToRemoveList.remove(checkable)
                        checkable.isChecked.value = true
                        selectionToRemove.value = newToRemoveList
                        itemsSelectedAmt.value = itemsSelectedAmt.value!! + 1
                    }
                } else {
                    newToRemoveList.add(checkable)
                    checkable.isChecked.value = false
                    selectionToRemove.value = newToRemoveList
                    itemsSelectedAmt.value = itemsSelectedAmt.value!! - 1
                }
            } else {
                val newToAddList = selectionToAdd.value!!.mutableCopyOf()
                if (selectionToAdd.value!!.contains(checkable)) {
                    newToAddList.remove(checkable)
                    checkable.isChecked.value = false
                    selectionToAdd.value = newToAddList
                    itemsSelectedAmt.value = itemsSelectedAmt.value!! - 1
                } else {
                    if (itemsSelectedAmt.value!! < choiceLimit) {
                        newToAddList.add(checkable)
                        checkable.isChecked.value = true
                        selectionToAdd.value = newToAddList
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
        selectionToAdd.value = mutableListOf<T>()
    }

    override fun canSelectMore(): Boolean {
        return ((initialSelection.size
                + selectionToAdd.value!!.size
                - selectionToRemove.value!!.size)

                < (choiceLimit.value ?: Int.MAX_VALUE))
    }
}