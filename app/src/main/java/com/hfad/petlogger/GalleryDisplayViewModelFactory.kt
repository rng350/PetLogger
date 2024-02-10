package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.fetchers.LinkedEntityFetcher

class GalleryDisplayViewModelFactory(private val associatedID: Long, private val fetcher: LinkedEntityFetcher<Photo>)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryDisplayViewModel::class.java)) {
            return GalleryDisplayViewModel(associatedID, fetcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}