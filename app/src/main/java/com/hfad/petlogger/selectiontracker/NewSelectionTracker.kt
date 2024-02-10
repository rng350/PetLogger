package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.LiveData
import com.hfad.petlogger.CheckableItem

class NewSelectionTracker<T>(override val choiceLimit: Int? = null): SelectionTracker<T> {
    private val tracker: NewSelectionTrackerInterface<CheckableItem<T>, T> =
        if (choiceLimit == 1) NewSelectionTrackerSinglePick<CheckableItem<T>, T>()
        else NewSelectionTrackerMultiPick<CheckableItem<T>, T>(choiceLimit)
    override val itemsSelectedAmt: LiveData<Int>
        get() = tracker.itemsSelectedAmt
    override val selectionToAdd: LiveData<MutableList<CheckableItem<T>>>
        get() = tracker.selection

    init {
        choiceLimit?.let {
            check(it > 0)
        }
    }

    override fun toggle(checkable: CheckableItem<T>) {
        if (tracker.selection.value!!.contains(checkable)) {
            tracker.remove(checkable)
        } else {
            tracker.add(checkable)
        }
    }

    fun add(item: CheckableItem<T>): Boolean {
        return tracker.add(item)
    }

    fun remove(item: CheckableItem<T>): Boolean {
        return tracker.remove(item)
    }

    override fun clear() {
        tracker.clear()
    }

    override fun canSelectMore(): Boolean {
        return tracker.canSelectMore()
    }
}