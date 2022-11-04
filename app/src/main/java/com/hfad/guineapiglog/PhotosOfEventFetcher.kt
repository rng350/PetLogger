package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PhotosOfEventFetcher(private val eventDao: EventDao): LinkedEntityFetcher<Photo> {
    override fun fetch(viewModel: ViewModel, id: Long, listToFill: MutableLiveData<List<Photo>>) {
        viewModel.viewModelScope.launch {
            val fetchedPhotos = async {eventDao.fetchPhotosOfEvent(id)}
            listToFill.value = fetchedPhotos.await()
        }
    }
}