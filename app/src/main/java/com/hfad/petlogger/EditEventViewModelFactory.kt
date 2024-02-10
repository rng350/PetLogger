package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.PetDao

class EditEventViewModelFactory(val eventID: Long, val eventDao: EventDao, val petDao: PetDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
            return EditEventViewModel(eventID, eventDao, petDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}