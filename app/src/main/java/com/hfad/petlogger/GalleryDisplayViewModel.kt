package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.fetchers.LinkedEntityFetcher
import com.hfad.petlogger.util.Navigator

class GalleryDisplayViewModel(associatedID: Long, fetcher: LinkedEntityFetcher<Photo>): ViewModel() {
    val photos = MutableLiveData(listOf<Photo>())
    val photoNavigator = Navigator()

    init {
        fetcher.fetch(this, associatedID, photos)
    }
}