package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateful.GetAllPetsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PetListViewModel(getAllPets: GetAllPetsForDisplayUseCase) : ViewModel() {
    val pets: StateFlow<List<PetWithProfilePic>> = getAllPets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf<PetWithProfilePic>()
        )
    val petNavigator = Navigator()

    companion object {
        fun provideFactory(getAllPets: GetAllPetsForDisplayUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetListViewModel::class.java)) {
                    return PetListViewModel(getAllPets) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}