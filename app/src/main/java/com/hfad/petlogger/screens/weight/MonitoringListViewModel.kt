package com.hfad.petlogger.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.weights.WeightForList
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitoringListViewModel(private val getWeightsUseCase: GetItemsUseCase<WeightForList>) : ViewModel() {
    private val _weights: MutableStateFlow<List<WeightForList>> = MutableStateFlow<List<WeightForList>>(listOf())
    val weights: StateFlow<List<WeightForList>> = _weights.asStateFlow()
    val weightNavigator = Navigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedWeights = getWeightsUseCase()
            _weights.update { it + loadedWeights }
            /*_weights.value.map {
                Log.d("WeightListVM", "WeightId: ${it.weightId}, WeightDate&Time: ${it.weightDate},${it.weightTime}, PetName: ${it.weightPetName}, Grams: ${it.weightGramsAmt}")
            }*/
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getWeightsUseCase.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }
    companion object {
        fun provideFactory(getWeightsUseCase: GetItemsUseCase<WeightForList>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MonitoringListViewModel::class.java)) {
                    return MonitoringListViewModel(getWeightsUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}