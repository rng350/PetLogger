package com.hfad.guineapiglog.photoselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.CheckableItem
import com.hfad.guineapiglog.EventDao
import com.hfad.guineapiglog.fetchers.Fetcher
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.selectiontracker.SharedCounterSelectionTracker
import com.hfad.guineapiglog.selectiontracker.VariableSelectionMode

class GalleryEditDisplayViewModel(val associatedID: MutableLiveData<Long>,
                                  val eventDao: EventDao,
                                  choiceLimit: Int) : ViewModel() {
    val oldPhotosAssociated = MutableLiveData<List<CheckableItem<Photo>>>()
    val photosPickedSharedCounter = MutableLiveData<Int>(0)
    val oldPhotosAssociatedTracker = SharedCounterSelectionTracker<Photo>(
        choiceLimit = choiceLimit,
        sharedCounter = photosPickedSharedCounter,
        VariableSelectionMode.SUBTRACTIVE)
    val newPhotosAssociatedTracker = SharedCounterSelectionTracker<Photo>(
        choiceLimit = choiceLimit,
        sharedCounter = photosPickedSharedCounter,
        VariableSelectionMode.CUMULATIVE)

    fun initOldPhotosAssociated() {
        Fetcher.fetchCheckablePhotosOfEvent(viewModelScope, oldPhotosAssociated, eventDao, associatedID.value!!)
    }

    fun setupSharedCounterSize() {
        oldPhotosAssociatedTracker.setupInitialSize(oldPhotosAssociated.value!!.size)
    }
}