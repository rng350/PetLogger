package com.hfad.petlogger.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.weights.data.WeightForList
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.common.util.NewEntityNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitoringListViewModel(
    private val getInitialWeightsUseCase: GetItemsUseCase<WeightForList>,
    private val getSearchedWeightsUseCase: GetSearchedItemsUseCase<WeightForList>
) : ViewModel() {
    private val _weights: MutableStateFlow<List<WeightForList>> = MutableStateFlow<List<WeightForList>>(listOf())
    private var currentWeightGetter: GetItemsUseCase<WeightForList> = getInitialWeightsUseCase
    val weights: StateFlow<List<WeightForList>> = _weights.asStateFlow()
    val weightNavigator = Navigator()
    val newWeightNavigator = NewEntityNavigator()
    private var isLoading: Boolean = false
    init {
        reload()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedWeights = currentWeightGetter()
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
        return getInitialWeightsUseCase.onLastPage
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
            getSearchedWeightsUseCase.changeSearchQueryAndResetCurrentPoint(query)
            currentWeightGetter = getSearchedWeightsUseCase
        } else {
            currentWeightGetter = getInitialWeightsUseCase
            currentWeightGetter.resetCurrentPoint()
        }
        reload()
    }
    companion object {
        fun provideFactory(getInitialWeightsUseCase: GetItemsUseCase<WeightForList>, getSearchedWeightsUseCase: GetSearchedItemsUseCase<WeightForList>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MonitoringListViewModel::class.java)) {
                    return MonitoringListViewModel(getInitialWeightsUseCase, getSearchedWeightsUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}