package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao

class ViewWeightViewModelFactory(val weightDao: WeightDao, val petDao: PetDao, val weightId: Long): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewWeightViewModel::class.java)) {
            return ViewWeightViewModel(weightDao, petDao, weightId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}