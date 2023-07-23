package com.hfad.guineapiglog.photoselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.guineapiglog.dao.EventDao

class GalleryEditDisplayViewModelFactory(private val associatedID: MutableLiveData<Long>,
                                         private val eventDao: EventDao,
                                         private val choiceLimit: Int) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryEditDisplayViewModel::class.java)) {
            return GalleryEditDisplayViewModel(associatedID, eventDao, choiceLimit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}