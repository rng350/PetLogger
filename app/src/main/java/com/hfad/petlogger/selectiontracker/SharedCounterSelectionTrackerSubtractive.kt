package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.mutableCopyOf

// a tracker whose selection size may be affected by another tracker's counter
// making a selection in this one subtracts from a shared counter
// conversely, unchecking decrements from the shared counter
class SharedCounterSelectionTrackerSubtractive<T: CheckableItem<U>, U>(override val choiceLimit: Int?,
                                                                       override val sharedCounter: MutableLiveData<Int>
                                                                       ): SharedCounterSelectionTrackerInterface<T,U> {
    override val selection = MutableLiveData(mutableListOf<T>())

    override fun add(item: T): Boolean {
        //old
        //selection.value!!.add(item)

        //new start
        val newList = selection.value!!.mutableCopyOf()
        newList.add(item)
        selection.value = newList
        //new end
        item.isChecked.value = true
        sharedCounter.value = sharedCounter.value!! - 1
        return true
    }

    override fun remove(item: T): Boolean {
        if (canUncheckMore()) {
            //old
            //val removed = selection.value!!.remove(item)

            //new start
            val newList = selection.value!!.mutableCopyOf()
            val removed = newList.remove(item)
            selection.value = newList
            //new end

            item.isChecked.value = false
            sharedCounter.value = sharedCounter.value!! + 1
            return removed
        }
        return false
    }

    override fun clear() {
        if (selection.value!!.size < ((choiceLimit ?: Int.MAX_VALUE) - sharedCounter.value!!)) {
            for (item in selection.value!!) {
                item.isChecked.value = false
            }
            sharedCounter.value = sharedCounter.value!! + selection.value!!.size
            selection.value = mutableListOf<T>()
        }
    }

    override fun canSelectMore(): Boolean {
        return true
    }

    override fun canUncheckMore(): Boolean {
        return (sharedCounter.value!! < (choiceLimit ?: Int.MAX_VALUE))
    }

    override fun toggle(item: T) {
        if (selection.value!!.contains(item)) {
            remove(item)
        } else {
            add(item)
        }
    }
}