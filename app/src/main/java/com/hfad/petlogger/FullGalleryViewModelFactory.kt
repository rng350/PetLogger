package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.PhotoDao

class FullGalleryViewModelFactory(private val photoDao: PhotoDao)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FullGalleryViewModel::class.java)) {
            return FullGalleryViewModel(photoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}