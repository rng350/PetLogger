package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

// put in viewmodel
class SelectionTrackerSinglePick<T: CheckableItem<U>, U>(): SelectionTracker<T, U> {
    override var selection = MutableLiveData(mutableListOf<T>())

    override fun add(item: T): Boolean {
        for (old in selection.value!!) {
            old.isChecked = false
        }
        item.isChecked = true
        selection.value = mutableListOf<T>(item)
        return true
    }

    override fun remove(item: T): Boolean {
        if (selection.value!!.contains(item)) {
            item.isChecked = false
            selection.value = mutableListOf<T>()
            return true
        }
        return false
    }

    override fun clear() {
        for (item in selection.value!!) {
            item.isChecked = false
        }
        selection.value!!.clear()
        selection.value = selection.value
    }
}