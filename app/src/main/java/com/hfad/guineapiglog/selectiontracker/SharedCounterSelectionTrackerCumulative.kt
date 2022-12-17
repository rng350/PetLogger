package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem
import com.hfad.guineapiglog.mutableCopyOf

// a tracker whose selection size may be affected by another tracker's counter
// making a selection in this one increments the shared counter
class SharedCounterSelectionTrackerCumulative<T: CheckableItem<U>, U>(override val choiceLimit: Int?,
                                                                      override val sharedCounter: MutableLiveData<Int>
                                                                      ): SharedCounterSelectionTrackerInterface<T,U> {
    override val selection = MutableLiveData(mutableListOf<T>())

    override fun add(item: T): Boolean {
        if (canSelectMore()) {
            // old
            //selection.value!!.add(item)

            // new start
            val newList = selection.value!!.mutableCopyOf()
            newList.add(item)
            selection.value = newList
            // new end

            sharedCounter.value = sharedCounter.value!! + 1
            selection.value = selection.value
            item.isChecked.value = true
            return true
        }
        return false
    }

    override fun remove(item: T): Boolean {
        //old
        //val removed = selection.value!!.remove(item)

        //new start
        val newList = selection.value!!.mutableCopyOf()
        val removed = newList.remove(item)
        selection.value = newList
        //new end

        if (removed) {
            item.isChecked.value = false
            //old
            //selection.value = selection.value
            sharedCounter.value = sharedCounter.value!! - 1
        }
        return removed
    }

    override fun clear() {
        for (item in selection.value!!) {
            item.isChecked.value = false
        }
        val selectionSize = selection.value!!.size
        sharedCounter.value = sharedCounter.value!! - selectionSize
        selection.value = mutableListOf<T>()
    }

    override fun canSelectMore(): Boolean {
        return sharedCounter.value!! < (choiceLimit ?: Int.MAX_VALUE)
    }

    override fun canUncheckMore(): Boolean {
        return true
    }

    override fun toggle(item: T) {
        if (selection.value!!.contains(item)) {
            remove(item)
        } else {
            add(item)
        }
    }
}