package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryDisplayViewModel(associatedID: Long, fetcher: LinkedEntityFetcher<Photo>): ViewModel() {
    val photos = MutableLiveData(listOf<Photo>())
    val photoNavigator = Navigator()

    init {
        fetcher.fetch(this, associatedID, photos)
    }
}