package com.hfad.petlogger.selectiontracker

import androidx.lifecycle.LiveData
import com.hfad.petlogger.CheckableItem

interface SelectionTracker<T> {
    val choiceLimit: Int?
    val itemsSelectedAmt: LiveData<Int>
    val selectionToAdd: LiveData<MutableList<CheckableItem<T>>>

    fun toggle(checkable: CheckableItem<T>)
    fun clear()
    fun canSelectMore(): Boolean
}