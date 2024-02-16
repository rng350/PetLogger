package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator

class ViewWeightViewModel(val weightDao: WeightDao, val weightId: Long): ViewModel() {
    val weight = MutableLiveData<Weight>()
    val weightNavigator = Navigator()
    init {
        Fetcher.fetchWeight(this, weight, weightDao, weightId)
    }
}