package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Dao

class GalleryDisplayViewModelFactory(private val associatedID: Long, private val fetcher: LinkedEntityFetcher<Photo>)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryDisplayViewModel::class.java)) {
            return GalleryDisplayViewModel(associatedID, fetcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}