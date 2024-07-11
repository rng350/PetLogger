package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.entities.WeightWithPetName
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.photodisplay.stateful.GetAllWeightsWithPetInfoForDisplayUseCase
import com.hfad.petlogger.repositories.WeightRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonitoringListViewModel(getWeightsUseCase: GetAllWeightsWithPetInfoForDisplayUseCase) : ViewModel() {
    val weights: StateFlow<List<WeightForList>> = getWeightsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf()
    )
    val weightNavigator = Navigator()

    companion object {
        fun provideFactory(getWeightsUseCase: GetAllWeightsWithPetInfoForDisplayUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MonitoringListViewModel::class.java)) {
                    return MonitoringListViewModel(getWeightsUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}