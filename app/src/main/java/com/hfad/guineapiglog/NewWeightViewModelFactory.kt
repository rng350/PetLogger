package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NewWeightViewModelFactory (val weightDao: WeightDao, val petDao: PetDao, val petId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewWeightViewModel::class.java)) {
            return NewWeightViewModel(weightDao, petDao, petId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}