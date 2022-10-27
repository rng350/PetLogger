package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryViewModel(val associatedIDType: AssociatedType, val choiceLimit: Int) : ViewModel() {
    val allExternalPhotos = MutableLiveData(listOf<CheckableItem<Photo>>())
    val photosSelected: SelectionTracker<CheckableItem<Photo>, Photo> =
        if (choiceLimit == 1) SelectionTrackerSinglePick<CheckableItem<Photo>, Photo>()
        else SelectionTrackerMultiPick<CheckableItem<Photo>, Photo>(choiceLimit)
    // val videosSelected = MutableLiveData<MutableListOf<Video>()
    var hasExternalReadPermission = MutableLiveData<Boolean>(false)
    val isExpanded = MutableLiveData<Boolean>(false)
    val photosSelectedAmt = MutableLiveData<Int>(0)


    init {
        check(choiceLimit > 0)
    }

    fun toggle(photo: CheckableItem<Photo>) {
        if (photosSelected.selection.value!!.contains(photo)) {
            // deselect photo
            if (photosSelected.remove(photo))
                photosSelectedAmt.value = photosSelectedAmt.value!! - 1
        } else {
            // select photo
            if (photosSelected.add(photo)) {
                photosSelectedAmt.value = photosSelectedAmt.value!! + 1
            }
        }
    }

    fun canSelectMore(): Boolean {
        return (choiceLimit == 1) || (photosSelected.selection.value!!.size < choiceLimit)
    }

    // TODO: Implement
    fun submitSelection() {
        // 1. save to internal storage
        // 2. save URIs to database
        // 3. save photo & ID association
    }
}