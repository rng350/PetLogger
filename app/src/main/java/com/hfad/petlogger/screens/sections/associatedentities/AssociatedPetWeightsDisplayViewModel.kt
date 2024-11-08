package com.hfad.petlogger.screens.sections.associatedentities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedPetWeightsDisplayViewModel(private val getWeights: GetItemsUseCase<Weight>): ViewModel() {
    private val _weights: MutableStateFlow<List<Weight>> = MutableStateFlow<List<Weight>>(listOf())
    val weights: StateFlow<List<Weight>> get() = _weights.asStateFlow()
    val weightNavigator = Navigator()
    private var isLoading: Boolean = false

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedWeights = getWeights()
            Log.d("AssocWeightsVM", "Loaded Events Size: ${loadedWeights.size}")
            Log.d("AssocWeightsVM", "List Size Before: ${weights.value.size}")
            _weights.update { it + loadedWeights }
            Log.d("AssocWeightsVM", "List Size After: ${weights.value.size}")
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getWeights.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    companion object {
        fun provideFactory(getWeights: GetItemsUseCase<Weight>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPetWeightsDisplayViewModel::class.java)) {
                    return AssociatedPetWeightsDisplayViewModel(getWeights) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}