package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FullGalleryViewModel(photoDao: PhotoDao) : ViewModel() {
    val photos = MutableLiveData<List<Photo>>()
    val photoNavigator = Navigator()
    init {
        Fetcher.fetchAllPhotos(viewModelScope, photos, photoDao)
    }
}