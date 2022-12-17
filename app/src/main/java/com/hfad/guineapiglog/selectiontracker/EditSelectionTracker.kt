package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem

/*
    1. fetch pets from viewmodel
    2. observer on petpetwithphoto, set to call
    -> 1. initialize selectionedittracker
    -> 2. initialize checkableitem petwithphoto, use shouldbechecked function from
    use that last checkable petwithphoto list for recycler view
*/

class EditSelectionTracker<T>(override val choiceLimit: Int?): SelectionTracker<T> {
    private val tracker: EditSelectionTrackerInterface<CheckableItem<T>, T> =
        if (choiceLimit == 1) EditSelectionTrackerSinglePick<CheckableItem<T>, T>()
        else EditSelectionTrackerMultiPick<CheckableItem<T>, T>(choiceLimit)
    override val selectionToAdd: LiveData<MutableList<CheckableItem<T>>>
        get() = tracker.selectionToAdd
    val selectionToRemove: LiveData<MutableList<CheckableItem<T>>>
        get() = tracker.selectionToRemove
    override val itemsSelectedAmt: LiveData<Int>
        get() = tracker.itemsSelectedAmt

    fun initializeSelection(items: List<T>) {
        tracker.initializeSelection(items)
    }

    override fun toggle(checkable: CheckableItem<T>) {
        tracker.toggle(checkable)
    }

    override fun clear() {
        tracker.clear()
    }

    override fun canSelectMore(): Boolean {
        return tracker.canSelectMore()
    }

    fun inInitialSelection(item: T): Boolean {
        return tracker.inInitialSelection(item)
    }
}