package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewPetViewModelFactory (val petDao: PetDao, val weightDao: WeightDao, val petID: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewPetViewModel::class.java)) {
            return ViewPetViewModel(petDao, weightDao, petID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}