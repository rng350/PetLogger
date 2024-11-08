package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem

interface SharedCounterSelectionTrackerInterface<T: CheckableItem<U>, U> {
    val choiceLimit: Int?
    val sharedCounter: MutableLiveData<Int>
    val selection: MutableLiveData<MutableList<T>>
    fun toggle(item: T)
    fun add(item: T): Boolean
    fun remove(item: T): Boolean
    fun clear()
    fun canSelectMore(): Boolean
    fun canUncheckMore(): Boolean
}