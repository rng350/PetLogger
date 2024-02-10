package com.hfad.petlogger.photoselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.selectiontracker.SharedCounterSelectionTracker
import com.hfad.petlogger.selectiontracker.VariableSelectionMode

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