package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem
import com.hfad.guineapiglog.mutableCopyOf

// put in viewmodel
// choiceLimit being null means no choice limits, aka infinite picks
class NewSelectionTrackerMultiPick<T: CheckableItem<U>, U>(val choiceLimit: Int? = null): NewSelectionTrackerInterface<T, U> {
    override var selection = MutableLiveData(mutableListOf<T>())
    private val _itemsSelectedAmt = MutableLiveData<Int>(0)
    override val itemsSelectedAmt: LiveData<Int>
        get() = _itemsSelectedAmt

    init {
        choiceLimit?.let {
            check (it > 0)
        }
    }

    override fun add(item: T): Boolean {
        // choiceLimit ?: Int.MAX_VALUE is to account for infinite choices option
        if (canSelectMore()) {
            val newList = selection.value!!.mutableCopyOf()
            newList.add(item)
            item.isChecked.value = true
            selection.value = newList
            _itemsSelectedAmt.value = _itemsSelectedAmt.value!! + 1
            return true
        }
        return false
    }

    override fun remove(item: T): Boolean {
        val newList = selection.value!!.mutableCopyOf()
        val removed = newList.remove(item)
        item.isChecked.value = false
        selection.value = newList
        _itemsSelectedAmt.value = _itemsSelectedAmt.value!! - 1
        return removed
    }

    override fun clear() {
        for (item in selection.value!!) {
            item.isChecked.value = false
        }
        selection.value = mutableListOf<T>()
        _itemsSelectedAmt.value = 0
    }

    override fun canSelectMore(): Boolean {
        choiceLimit?.let {
            return (selection.value!!.size < it)
        }
        // no limits
        return true
    }
}