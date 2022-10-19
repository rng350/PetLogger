package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GalleryViewModelFactory(private val associatedIDType: AssociatedType, private val choiceLimit: Int)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(associatedIDType, choiceLimit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}