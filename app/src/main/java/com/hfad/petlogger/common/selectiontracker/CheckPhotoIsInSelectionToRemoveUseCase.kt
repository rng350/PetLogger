package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.photos.Photo

class CheckPhotoIsInSelectionToRemoveUseCase: CheckItemIsInSelectionUseCase<Photo> {
    private var photosInSelection = LinkedHashMap<Long, CheckableItem<Photo>>()

    override fun containsItem(item: Photo): CheckableItem<Photo>? {
        return photosInSelection[item.id]
    }

    override fun resetToItemList(newList: List<Photo>) {
        photosInSelection.clear()
        photosInSelection.putAll(newList.map { CheckableItem(it, MutableLiveData(true)) }.associateBy { it.item.id })
    }

    override fun addItems(newItems: List<Photo>) {
        photosInSelection.putAll(newItems.map { CheckableItem(it, MutableLiveData(true)) }.associateBy { it.item.id })
    }

    override fun addItem(newItem: Photo) {
        photosInSelection.putAll(mapOf(Pair(newItem.id, CheckableItem(newItem, MutableLiveData(true)))))
    }

    override fun removeItems(itemsToRemove: List<Photo>): List<CheckableItem<Photo>> {
        val removedPhotos = mutableListOf<CheckableItem<Photo>>()
        for (item in itemsToRemove) {
            val removedPhoto = photosInSelection.remove(item.id)
            removedPhoto?.let {
                it.isChecked.value = false
                removedPhotos.add(it)
            }
        }
        return removedPhotos.toList()
    }

    override fun removeItem(itemToRemove: Photo): CheckableItem<Photo>? {
        val removedPhoto = photosInSelection.remove(itemToRemove.id)
        removedPhoto?.let {
            it.isChecked.value = false
        }
        return removedPhoto
    }

    override fun containsCheckableItem(item: CheckableItem<Photo>): CheckableItem<Photo>? {
        return photosInSelection.get(item.item.id)
    }

    override fun resetToCheckableItemList(newList: List<CheckableItem<Photo>>) {
        photosInSelection.clear()
        photosInSelection.putAll(newList.onEach { it.isChecked.value = true }.associateBy { it.item.id })
    }

    override fun addCheckableItems(newItems: List<CheckableItem<Photo>>) {
        photosInSelection.putAll(newItems.onEach { it.isChecked.value = true }.associateBy { it.item.id })
    }

    override fun addCheckableItem(newItem: CheckableItem<Photo>) {
        newItem.isChecked.value = true
        photosInSelection.put(newItem.item.id, newItem)
    }

    override fun removeCheckableItems(itemsToRemove: List<CheckableItem<Photo>>): List<CheckableItem<Photo>> {
        val removedPhotos = mutableListOf<CheckableItem<Photo>>()
        for (item in itemsToRemove) {
            val removedPhoto = photosInSelection.remove(item.item.id)
            removedPhoto?.let {
                it.isChecked.value = false
                removedPhotos.add(it)
            }
        }
        return removedPhotos.toList()
    }

    override fun removeCheckableItem(itemToRemove: CheckableItem<Photo>): CheckableItem<Photo>? {
        val removedPhoto = photosInSelection.remove(itemToRemove.item.id)
        removedPhoto?.let {
            it.isChecked.value = false
        }
        return removedPhoto
    }

    override fun getList(): List<CheckableItem<Photo>> {
        return photosInSelection.values.toList()
    }
}