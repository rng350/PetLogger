package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem

interface NewSelectionTrackerInterface<T: CheckableItem<U>, U> {
    var selection: MutableLiveData<MutableList<T>>
    val itemsSelectedAmt: LiveData<Int>
    fun add(item: T): Boolean
    fun remove(item: T): Boolean
    fun clear()
    fun canSelectMore(): Boolean
}