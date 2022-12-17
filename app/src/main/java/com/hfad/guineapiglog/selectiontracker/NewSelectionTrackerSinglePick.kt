package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem

// put in viewmodel
class NewSelectionTrackerSinglePick<T: CheckableItem<U>, U>(): NewSelectionTrackerInterface<T, U> {
    override var selection = MutableLiveData(mutableListOf<T>())
    private val _itemsSelectedAmt = MutableLiveData<Int>(0)
    override val itemsSelectedAmt: LiveData<Int>
        get() = _itemsSelectedAmt

    override fun add(item: T): Boolean {
        for (old in selection.value!!) {
            old.isChecked.value = false
        }
        item.isChecked.value = true
        selection.value = mutableListOf<T>(item)
        _itemsSelectedAmt.value = 1
        return true
    }

    override fun remove(item: T): Boolean {
        if (selection.value!!.contains(item)) {
            item.isChecked.value = false
            selection.value = mutableListOf<T>()
            _itemsSelectedAmt.value = 0
            return true
        }
        return false
    }

    override fun clear() {
        for (item in selection.value!!) {
            item.isChecked.value = false
        }
        selection.value = mutableListOf<T>()
        _itemsSelectedAmt.value = 0
    }

    override fun canSelectMore(): Boolean {
        return true
    }

}