package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

interface SelectionTrackerInterface<T: CheckableItem<U>, U> {
    var selection: MutableLiveData<MutableList<T>>
    fun add(item: T): Boolean
    fun remove(item: T): Boolean
    fun clear()
    fun canSelectMore(): Boolean
}