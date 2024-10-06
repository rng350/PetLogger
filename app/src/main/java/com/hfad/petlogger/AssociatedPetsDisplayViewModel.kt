package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedPetsDisplayViewModel(private val getAssociatedPets: GetItemsUseCase<PetWithProfilePic>) : ViewModel() {
    private val _pets: MutableStateFlow<List<PetWithProfilePic>> = MutableStateFlow<List<PetWithProfilePic>>(listOf())
    val pets: StateFlow<List<PetWithProfilePic>> = _pets.asStateFlow()
    val navigator = Navigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedPets = getAssociatedPets()
            _pets.update { it + loadedPets }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAssociatedPets.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    companion object {
        fun provideFactory(getAssociatedPets: GetItemsUseCase<PetWithProfilePic>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPetsDisplayViewModel::class.java)) {
                    return AssociatedPetsDisplayViewModel(getAssociatedPets) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}