package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

// put in viewmodel
// choiceLimit being null means no choice limits, aka infinite picks
class SelectionTrackerMultiPick<T: CheckableItem<U>, U>(val choiceLimit: Int? = null): SelectionTrackerInterface<T, U> {
    override var selection = MutableLiveData(mutableListOf<T>())

    init {
        choiceLimit?.let {
            check (it > 0)
        }
    }

    override fun add(item: T): Boolean {
        // choiceLimit ?: Int.MAX_VALUE is to account for infinite choices option
        if (selection.value!!.size < (choiceLimit ?: Int.MAX_VALUE)) {
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

    override fun canSelectMore(): Boolean {
        choiceLimit?.let {
            return (selection.value!!.size < it)
        }
        // no limits
        return true
    }
}