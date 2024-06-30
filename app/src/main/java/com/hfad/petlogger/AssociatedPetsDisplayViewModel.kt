package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AssociatedPetsDisplayViewModel(getAssociatedPets: GetItemsForDisplayUseCase<PetWithProfilePic>) : ViewModel() {
    val pets: StateFlow<List<PetWithProfilePic>> = getAssociatedPets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf<PetWithProfilePic>()
        )
    val navigator = Navigator()

    companion object {
        fun provideFactory(getAssociatedPets: GetItemsForDisplayUseCase<PetWithProfilePic>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPetsDisplayViewModel::class.java)) {
                    return AssociatedPetsDisplayViewModel(getAssociatedPets) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}