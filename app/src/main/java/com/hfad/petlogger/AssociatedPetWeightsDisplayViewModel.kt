package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.photodisplay.stateful.GetAssociatedItemsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AssociatedPetWeightsDisplayViewModel(private val getWeights: GetAssociatedItemsForDisplayUseCase<Weight>): ViewModel() {
    val weights: StateFlow<List<Weight>> = getWeights().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf<Weight>()
    )
    val weightNavigator = Navigator()

    companion object {
        fun provideFactory(getWeights: GetAssociatedItemsForDisplayUseCase<Weight>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPetWeightsDisplayViewModel::class.java)) {
                    return AssociatedPetWeightsDisplayViewModel(getWeights) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}