package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateful.GetAllPetsForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetListViewModel(private val getPets: GetItemsUseCase<PetWithProfilePic>) : ViewModel() {
    private val _pets: MutableStateFlow<List<PetWithProfilePic>> = MutableStateFlow(listOf())
    val pets: StateFlow<List<PetWithProfilePic>> = _pets.asStateFlow()
    val petNavigator = Navigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedPets = getPets()
            _pets.update { it + loadedPets }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getPets.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }
    companion object {
        fun provideFactory(getPets: GetItemsUseCase<PetWithProfilePic>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetListViewModel::class.java)) {
                    return PetListViewModel(getPets) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}