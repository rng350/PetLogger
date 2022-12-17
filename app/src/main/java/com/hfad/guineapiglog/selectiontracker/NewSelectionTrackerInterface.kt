package com.hfad.guineapiglog.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.CheckableItem

interface NewSelectionTrackerInterface<T: CheckableItem<U>, U> {
    var selection: MutableLiveData<MutableList<T>>
    val itemsSelectedAmt: LiveData<Int>
    fun add(item: T): Boolean
    fun remove(item: T): Boolean
    fun clear()
    fun canSelectMore(): Boolean
}