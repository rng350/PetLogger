package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.guineapiglog.dao.EventDao
import com.hfad.guineapiglog.dao.PetDao

class EditEventViewModelFactory(val eventID: Long, val eventDao: EventDao, val petDao: PetDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
            return EditEventViewModel(eventID, eventDao, petDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}