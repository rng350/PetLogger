package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem

interface EditSelectionTrackerInterface<T: CheckableItem<U>, U> {
    val selectionToAdd: MutableLiveData<MutableList<T>>
    val selectionToRemove: MutableLiveData<MutableList<T>>
    val itemsSelectedAmt: MutableLiveData<Int>

    fun initializeSelection(selection: List<U>)
    fun toggle(checkable: T)
    fun inInitialSelection(item: U): Boolean
    fun canSelectMore(): Boolean
    fun clear()
}