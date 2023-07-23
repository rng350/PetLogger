package com.hfad.guineapiglog.fetchers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.dao.EventDao
import com.hfad.guineapiglog.entities.Photo
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