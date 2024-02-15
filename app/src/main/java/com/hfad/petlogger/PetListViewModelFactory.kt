package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.PetDao

class PetListViewModelFactory(private val petDao: PetDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetListViewModel::class.java)) {
            return PetListViewModel(petDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}