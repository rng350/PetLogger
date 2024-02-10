package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.EventDao

class ViewEventViewModelFactory (val eventDao: EventDao, val eventID: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewEventViewModel::class.java)) {
            return ViewEventViewModel(eventDao, eventID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}