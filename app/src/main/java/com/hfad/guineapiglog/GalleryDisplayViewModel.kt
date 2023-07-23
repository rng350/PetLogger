package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.fetchers.LinkedEntityFetcher
import com.hfad.guineapiglog.util.Navigator

class GalleryDisplayViewModel(associatedID: Long, fetcher: LinkedEntityFetcher<Photo>): ViewModel() {
    val photos = MutableLiveData(listOf<Photo>())
    val photoNavigator = Navigator()

    init {
        fetcher.fetch(this, associatedID, photos)
    }
}