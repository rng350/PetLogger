package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.guineapiglog.dao.EventDao
import com.hfad.guineapiglog.dao.EventPetDao
import com.hfad.guineapiglog.dao.PetDao

class NewEventViewModelFactory(private val eventDao: EventDao,
                               private val eventPetDao: EventPetDao,
                               private val petDao: PetDao
)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewEventViewModel::class.java)) {
            return NewEventViewModel(eventDao, eventPetDao, petDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}