package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GalleryViewModelFactory(private val entityLinker: EntityLinker, private val choiceLimit: Int, private val photoDao: PhotoDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(entityLinker, choiceLimit, photoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}