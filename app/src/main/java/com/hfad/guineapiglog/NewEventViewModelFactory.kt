package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NewEventViewModelFactory(private val eventDao: EventDao, private val eventPetDao: EventPetDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewEventViewModel::class.java)) {
            return NewEventViewModel(eventDao, eventPetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}