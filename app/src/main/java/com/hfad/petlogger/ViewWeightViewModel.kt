package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.util.Navigator

class ViewWeightViewModel(val weightDao: WeightDao, val petDao: PetDao, val weightId: Long): ViewModel() {
    val weight = MutableLiveData<Weight>()
    val prevWeight = MutableLiveData<Weight?>()
    val assocPet = MutableLiveData<Pet>()
    val weightNavigator = Navigator()
    init {
        Fetcher.fetchWeight(viewModelScope, weight, weightDao, weightId)
        //Fetcher.fetchWeightPrevWeightAndPet(viewModelScope, weight, prevWeight, assocPet, weightId, weightDao, petDao)
    }
}