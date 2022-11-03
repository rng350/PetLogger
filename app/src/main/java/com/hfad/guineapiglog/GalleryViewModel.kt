package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.*
import com.hfad.guineapiglog.databinding.GalleryPickerItemBinding
import kotlinx.coroutines.launch

class GalleryViewModel(val entityLinker: EntityLinker, val choiceLimit: Int, val photoDao: PhotoDao) : ViewModel() {
    val allExternalPhotos = MutableLiveData(listOf<CheckableItem<Photo>>())
    val photosSelected: SelectionTracker<CheckableItem<Photo>, Photo> =
        if (choiceLimit == 1) SelectionTrackerSinglePick<CheckableItem<Photo>, Photo>()
        else SelectionTrackerMultiPick<CheckableItem<Photo>, Photo>(choiceLimit)
    // val videosSelected = MutableLiveData<MutableListOf<Video>()
    var hasExternalReadPermission = MutableLiveData<Boolean>(false)
    val isExpanded = MutableLiveData<Boolean>(false)
    val photosSelectedAmt = MutableLiveData<Int>(0)
    val curSelectionFileSize = MutableLiveData<Double>(0.0)
    val freeSpace = MutableLiveData<Int>(0)
    val finalPhotoSelection = MutableLiveData<List<Photo>>(null)
    val associatedID = MediatorLiveData<Long>()
    private var photosInsertedToDB = false

    val selected = HashMap<GalleryPickerItemBinding, Observer<MutableList<CheckableItem<Photo>>>>()

    init {
        check(choiceLimit > 0)
    }

    fun toggle(photo: CheckableItem<Photo>) {
        if (photosSelected.selection.value!!.contains(photo)) {
            // deselect photo
            if (photosSelected.remove(photo)) {
                photosSelectedAmt.value = photosSelectedAmt.value!! - 1
                curSelectionFileSize.value = curSelectionFileSize.value!! - photo.item.size
            }
        } else {
            // select photo
            if (photosSelected.add(photo)) {
                if (choiceLimit > 1) {
                    photosSelectedAmt.value = photosSelectedAmt.value!! + 1
                    curSelectionFileSize.value = curSelectionFileSize.value!! + photo.item.size
                }
                else {
                    photosSelectedAmt.value = 1
                    curSelectionFileSize.value = photo.item.size
                }
            }
        }
    }

    fun canSelectMore(): Boolean {
        return (choiceLimit == 1) || (photosSelected.selection.value!!.size < choiceLimit)
    }

    fun onFinalPhotoSelectionUploaded() {
        for (photo in finalPhotoSelection.value!!) {
            viewModelScope.launch { photoDao.insert(photo) }
        }
        photosInsertedToDB = true
        associatePhotos()
    }

    fun associatePhotos() {
        Log.d("associating_photos", "got associated id? ${associatedID.value != null}, photos inserted? ${finalPhotoSelection.value != null}")
        // if other entity has already been inserted to database
        associatedID.value?.let { assocID ->
            if (photosInsertedToDB) {
                finalPhotoSelection.value?.let { photoList ->
                    for (photo in photoList) {
                        viewModelScope.launch {
                            entityLinker.associateWith(photo.id, assocID)
                        }
                    }
                }
                associatedID.value = null
                finalPhotoSelection.value = null
            }
        }
    }
}