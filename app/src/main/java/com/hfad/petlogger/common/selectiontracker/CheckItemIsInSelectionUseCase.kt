package com.hfad.petlogger.common.selectiontracker

import com.hfad.petlogger.common.CheckableItem

interface CheckItemIsInSelectionUseCase<T> {
    fun containsItem(item: T): CheckableItem<T>?
    fun containsCheckableItem(item: CheckableItem<T>): CheckableItem<T>?
    fun resetToItemList(newList: List<T>)
    fun resetToCheckableItemList(newList: List<CheckableItem<T>>)
    fun addItems(newItems: List<T>)
    fun addCheckableItems(newItems: List<CheckableItem<T>>)
    fun addItem(newItem: T)
    fun addCheckableItem(newItem: CheckableItem<T>)
    fun removeItems(itemsToRemove: List<T>): List<CheckableItem<T>>
    fun removeCheckableItems(itemsToRemove: List<CheckableItem<T>>): List<CheckableItem<T>>
    fun removeItem(itemToRemove: T): CheckableItem<T>?
    fun removeCheckableItem(itemToRemove: CheckableItem<T>): CheckableItem<T>?
    fun getList(): List<CheckableItem<T>>
}