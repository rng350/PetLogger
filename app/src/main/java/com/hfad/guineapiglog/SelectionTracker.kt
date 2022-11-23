package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SelectionTracker<T>(val choiceLimit: Int? = null) {
    private val tracker: SelectionTrackerInterface<CheckableItem<T>, T> =
        if (choiceLimit == 1) SelectionTrackerSinglePick<CheckableItem<T>, T>()
        else SelectionTrackerMultiPick<CheckableItem<T>, T>(choiceLimit)
    val selection: LiveData<MutableList<CheckableItem<T>>>
        get() = tracker.selection
    private var _itemsSelectedAmt = MutableLiveData<Int>(0)
    val itemsSelectedAmt: LiveData<Int>
        get() = _itemsSelectedAmt

    init {
        choiceLimit?.let {
            check(it > 0)
        }
    }

    fun toggle(item: CheckableItem<T>) {
        if (tracker.selection.value!!.contains(item)) {
            tracker.remove(item)
        } else {
            tracker.add(item)
        }
        _itemsSelectedAmt.value = tracker.selection.value!!.size
    }

    fun add(item: CheckableItem<T>): Boolean {
        val succeeded = tracker.add(item)
        _itemsSelectedAmt.value = tracker.selection.value!!.size
        return succeeded
    }

    fun remove(item: CheckableItem<T>): Boolean {
        val succeeded = tracker.remove(item)
        _itemsSelectedAmt.value = tracker.selection.value!!.size
        return succeeded
    }

    fun clear(item: CheckableItem<T>) {
        tracker.clear()
        _itemsSelectedAmt.value = tracker.selection.value!!.size
    }

    fun canSelectMore(): Boolean {
        return tracker.canSelectMore()
    }
}