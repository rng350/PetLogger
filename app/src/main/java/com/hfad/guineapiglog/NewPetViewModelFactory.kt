package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NewPetViewModelFactory(private val dao: PetDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewPetViewModel::class.java)) {
            return NewPetViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}