package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem

enum class VariableSelectionMode {
    CUMULATIVE,
    SUBTRACTIVE
}

// a tracker whose selection size may be affected by another tracker's counter
class SharedCounterSelectionTracker<T>(override val choiceLimit: Int?,
                                       val sharedCounter: MutableLiveData<Int>,
                                       variableSelectionMode: VariableSelectionMode
                                       ): SelectionTracker<T> {
    val tracker: SharedCounterSelectionTrackerInterface<CheckableItem<T>, T> =
        if (variableSelectionMode == VariableSelectionMode.CUMULATIVE) SharedCounterSelectionTrackerCumulative<CheckableItem<T>, T>(choiceLimit, sharedCounter)
        else SharedCounterSelectionTrackerSubtractive<CheckableItem<T>, T>(choiceLimit, sharedCounter)
    override val itemsSelectedAmt: LiveData<Int>
        get() = sharedCounter
    override val selectionToAdd: LiveData<MutableList<CheckableItem<T>>>
        get() = tracker.selection

    // meant for initializing
    fun setupInitialSize(amount: Int) {
        check(amount < (choiceLimit ?: Int.MAX_VALUE))
        sharedCounter.value = amount
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

    fun canUncheckMore(): Boolean {
        return tracker.canUncheckMore()
    }

    fun remove(checkable: CheckableItem<T>): Boolean {
        return tracker.remove(checkable)
    }
}