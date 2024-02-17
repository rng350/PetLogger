package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MonitoringListViewModel(weightDao: WeightDao, petDao: PetDao) : ViewModel() {
    val weights = MutableLiveData<MutableList<WeightWithPetName>>()
    val weightNavigator = Navigator()
    init {
        Fetcher.fetchAllWeightsWithPetNames(this, weights, weightDao, petDao)
    }
}