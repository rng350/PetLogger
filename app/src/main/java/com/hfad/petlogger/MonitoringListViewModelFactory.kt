package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao

class MonitoringListViewModelFactory(private val weightDao: WeightDao, private val petDao: PetDao)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitoringListViewModel::class.java)) {
            return MonitoringListViewModel(weightDao, petDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}