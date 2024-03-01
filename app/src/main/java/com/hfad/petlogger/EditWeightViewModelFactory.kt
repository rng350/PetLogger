package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao

class EditWeightViewModelFactory(val weightId: Long,
                                 val weightDao: WeightDao)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditWeightViewModel::class.java)) {
            return EditWeightViewModel(weightId, weightDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}