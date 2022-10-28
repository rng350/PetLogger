package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

// put in viewmodel
class SelectionTrackerMultiPick<T: CheckableItem<U>, U>(val choiceLimit: Int): SelectionTracker<T, U> {
    override var selection = MutableLiveData(mutableListOf<T>())

    init {
        check (choiceLimit > 0)
    }

    override fun add(item: T): Boolean {
        if (selection.value!!.size < choiceLimit) {
            selection.value!!.add(item)
            item.isChecked = true
            selection.value = selection.value
            return true
        }
        return false
    }

    override fun remove(item: T): Boolean {
        val removed = selection.value!!.remove(item)
        item.isChecked = false
        selection.value = selection.value
        return removed
    }

    override fun clear() {
        for (item in selection.value!!) {
            item.isChecked = false
        }
        selection.value!!.clear()
        selection.value = selection.value
    }
}