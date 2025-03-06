package com.hfad.petlogger.screens.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.pets.data.PetWithProfilePic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetListViewModel(
    private val getInitialPetList: GetItemsUseCase<PetWithProfilePic>,
    private val getSearchedPets: GetSearchedItemsUseCase<PetWithProfilePic>
) : ViewModel() {

    private var currentPetGetter: GetItemsUseCase<PetWithProfilePic> = getInitialPetList
    private val _pets: MutableStateFlow<List<PetWithProfilePic>> = MutableStateFlow(listOf())
    val pets: StateFlow<List<PetWithProfilePic>> = _pets.asStateFlow()
    val petNavigator = Navigator()
    private var isLoading: Boolean = false

    init {
        reload()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedPets = currentPetGetter()
            _pets.update { it + loadedPets }
            isLoading = false
        }
    }

    private fun reload() {
        viewModelScope.launch {
            isLoading = true
            val loadedPets = currentPetGetter()
            _pets.update { loadedPets }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return currentPetGetter.onLastPage
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
            getSearchedPets.changeSearchQueryAndResetCurrentPoint(query)
            currentPetGetter = getSearchedPets
        } else {
            currentPetGetter = getInitialPetList
            currentPetGetter.resetCurrentPoint()
        }
        reload()
    }
    companion object {
        fun provideFactory(getInitialPetList: GetItemsUseCase<PetWithProfilePic>, getSearchedPets: GetSearchedItemsUseCase<PetWithProfilePic>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetListViewModel::class.java)) {
                    return PetListViewModel(getInitialPetList, getSearchedPets) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}