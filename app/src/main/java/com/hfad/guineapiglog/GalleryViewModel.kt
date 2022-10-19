package com.hfad.guineapiglog

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryViewModel(val associatedIDType: AssociatedType, val choiceLimit: Int) : ViewModel() {
    val allExternalPhotos = MutableLiveData(listOf<Photo>())
    val somethingelseDELETEME = MutableLiveData(mutableListOf<Photo>())
    val photosSelected = MutableLiveData(mutableListOf<Photo>())
    // val videosSelected = MutableLiveData<MutableListOf<Video>()
    var hasExternalReadPermission = MutableLiveData<Boolean>(false)
    val isExpanded = MutableLiveData<Boolean>(false)

    init {
        check(choiceLimit > 0)
    }

    fun submitSelection() {
        // 1. save to internal storage
        // 2. save URIs to database
        // 3. save photo & ID association
    }

    private suspend fun loadPhotosFromExternalStorage() {
    }

    private suspend fun savePhotosToInternalStorage() {
    }
}