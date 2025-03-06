package com.hfad.petlogger.screens.sections.associatedentities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.common.util.NewEntityNavigator
import com.hfad.petlogger.weights.data.PetWeightForDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedPetWeightsDisplayViewModel(
    private val getInitialWeights: GetItemsUseCase<PetWeightForDisplay>,
    private val getSearchedWeights: GetSearchedItemsUseCase<PetWeightForDisplay>
): ViewModel() {
    private val _weights: MutableStateFlow<List<PetWeightForDisplay>> = MutableStateFlow<List<PetWeightForDisplay>>(listOf())
    private var currentWeightGetter: GetItemsUseCase<PetWeightForDisplay> = getInitialWeights
    val weights: StateFlow<List<PetWeightForDisplay>> = _weights.asStateFlow()
    val weightNavigator = Navigator()
    val newPetWeightNavigator = NewEntityNavigator()
    private var isLoading: Boolean = false
    init {
        reload()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedWeights = getInitialWeights()
            _weights.update { it + loadedWeights }
            isLoading = false
        }
    }

    private fun reload() {
        viewModelScope.launch {
            isLoading = true
            val loadedWeights = currentWeightGetter()
            _weights.update { loadedWeights }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getInitialWeights.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    fun onQueryTextSubmit(query: String?) {
        if (query != null) {
            reinitializeGetterType(query)
        }
    }

    fun onQueryTextChanged(newText: String?) {
        if (newText != null) {
            reinitializeGetterType(newText)
        }
    }

    private fun reinitializeGetterType(query: String) {
        if (query.isNotEmpty()) {
            getSearchedWeights.changeSearchQueryAndResetCurrentPoint(query)
            currentWeightGetter = getSearchedWeights
        } else {
            currentWeightGetter = getInitialWeights
            currentWeightGetter.resetCurrentPoint()
        }
        reload()
    }

    companion object {
        fun provideFactory(getInitialWeights: GetItemsUseCase<PetWeightForDisplay>, getSearchedWeights: GetSearchedItemsUseCase<PetWeightForDisplay>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPetWeightsDisplayViewModel::class.java)) {
                    return AssociatedPetWeightsDisplayViewModel(getInitialWeights, getSearchedWeights) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}