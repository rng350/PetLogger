package com.hfad.guineapiglog.photoselection

import android.util.Log
import androidx.lifecycle.*
import com.hfad.guineapiglog.CheckableItem
import com.hfad.guineapiglog.entitylinkers.EntityLinker
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.PhotoDao
import com.hfad.guineapiglog.databinding.GalleryPickerItemBinding
import com.hfad.guineapiglog.selectiontracker.SelectionTracker
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class GalleryViewModel(val entityLinker: EntityLinker,
                       val photoDao: PhotoDao,
                       val photosSelected: SelectionTracker<Photo>
                       ) : ViewModel() {
    val allExternalPhotos = MutableLiveData(listOf<CheckableItem<Photo>>())
    // val videosSelected = MutableLiveData<MutableListOf<Video>()
    var hasExternalReadPermission = MutableLiveData<Boolean>(false)
    val isExpanded = MutableLiveData<Boolean>(false)
    val curSelectionFileSize = MutableLiveData<Double>(0.0)
    val freeSpace = MutableLiveData<Int>(0)
    val finalPhotoSelection = MutableLiveData<List<Photo>>(null)
    val associatedID = MediatorLiveData<Long>()
    val allPhotosInsertedToDB = MutableLiveData<Boolean>(false)
    private val photosInsertedAmt = AtomicInteger(0)

    // for the recyclerview adapter
    val selected = HashMap<GalleryPickerItemBinding, Observer<Boolean>>()

    // TODO: delete this when can, function moved to selectiontracker
    fun toggle(photo: CheckableItem<Photo>) {
        photosSelected.toggle(photo)
    }

    // TODO: delete this when can, function moved to selectiontracker
    fun canSelectMore(): Boolean {
        return photosSelected.canSelectMore()
    }

    fun onFinalPhotoSelectionUploaded() {
        for (photo in finalPhotoSelection.value!!) {
            viewModelScope.launch {
                val inserted = async {
                    photoDao.insert(photo)
                    Log.d("photo_inserted_db", "${photo}")
                }
                inserted.await()
                if (photosInsertedAmt.incrementAndGet() == finalPhotoSelection.value!!.size) {
                    allPhotosInsertedToDB.value = true
                }
            }
        }
    }

    fun associatePhotos() {
        Log.d("associating_photos", "got associated id? ${associatedID.value != null}, photos inserted? ${finalPhotoSelection.value != null}")
        // if other entity has already been inserted to database
        associatedID.value?.let { assocID ->
            if (allPhotosInsertedToDB.value == true) {
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