package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditPetViewModelFactory(val petID: Long, val petDao: PetDao, val photoDao: PhotoDao, val eventDao: EventDao, val weightDao: WeightDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPetViewModel::class.java)) {
            return EditPetViewModel(petID, petDao, photoDao, eventDao, weightDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}