package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelFactory(private val petDao: PetDao, private val eventDao: EventDao, private val weightDao: WeightDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(petDao, eventDao, weightDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}