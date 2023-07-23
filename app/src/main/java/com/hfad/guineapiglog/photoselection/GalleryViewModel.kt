package com.hfad.guineapiglog.photoselection

import android.util.Log
import androidx.lifecycle.*
import com.hfad.guineapiglog.CheckableItem
import com.hfad.guineapiglog.entitylinkers.EntityLinker
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.dao.PhotoDao
import com.hfad.guineapiglog.databinding.GalleryPickerItemBinding
import com.hfad.guineapiglog.selectiontracker.SelectionTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

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
    val finalPhotoSelection = MutableLiveData<List<Photo>?>(null)
    val associatedID = MediatorLiveData<Long?>()
    val allPhotosInsertedToDB = MutableLiveData<Boolean>(false)

    // for the recyclerview adapter
    val selected = HashMap<GalleryPickerItemBinding, Observer<Boolean>>()

    fun toggle(photo: CheckableItem<Photo>) {
        photosSelected.toggle(photo)
    }

    fun canSelectMore(): Boolean {
        return photosSelected.canSelectMore()
    }

    fun onFinalPhotoSelectionUploaded() {
        viewModelScope.launch(Dispatchers.IO) {
            finalPhotoSelection.value?.map {photo ->
                async {
                    photoDao.insert(photo)
                    Log.d("photo_inserted_db", "${photo}")
                }
            }?.awaitAll()
            allPhotosInsertedToDB.postValue(true)
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

    fun insertPhotos() {
    }
}