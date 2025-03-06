package com.hfad.petlogger.screens.pet.viewpet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.pets.data.PetDetailsForDisplay
import com.hfad.petlogger.pets.domain.usecases.GetPetDetailsForDisplayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewPetViewModel (
    getPetDetails: GetPetDetailsForDisplayUseCase
): ViewModel() {
    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status.Loading)
    val status: StateFlow<Status> = _status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Status.Loading
        )
    private val _petDetails : MutableStateFlow<PetDetailsForDisplay> = MutableStateFlow<PetDetailsForDisplay>(
        PetDetailsForDisplay()
    )
    val petDetails: StateFlow<PetDetailsForDisplay> get() = _petDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PetDetailsForDisplay()
        )

    init {
        viewModelScope.launch {
            val fetchedPetDetails = getPetDetails()
            if (fetchedPetDetails is GetPetDetailsForDisplayUseCase.Result.Success) {
                _petDetails.value = fetchedPetDetails.fetchedPet
            }
            _status.value = Status.Loaded(fetchedPetDetails)
        }
    }

    sealed class Status {
        data object Loading: Status()
        data class Loaded(val result: GetPetDetailsForDisplayUseCase.Result): Status()
    }

    companion object {
        fun provideFactory(getPetDetails: GetPetDetailsForDisplayUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewPetViewModel::class.java)) {
                    return ViewPetViewModel(getPetDetails) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}