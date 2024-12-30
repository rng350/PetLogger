package com.hfad.petlogger.screens.pet.viewpet

import androidx.lifecycle.*
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetPeriodDisplayUseCase
import com.hfad.petlogger.pets.PetDetailsForDisplay
import com.hfad.petlogger.pets.usecases.GetPetDetailsUseCase
import com.hfad.petlogger.weights.PetWeightForDisplay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewPetViewModel (
    getPetDetails: GetPetDetailsUseCase
): ViewModel() {
    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status.Loading)
    val status: StateFlow<Status> = _status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Status.Loading
        )
    private val _petDetails : MutableStateFlow<PetDetailsForDisplay> = MutableStateFlow<PetDetailsForDisplay>(PetDetailsForDisplay())
    val petDetails: StateFlow<PetDetailsForDisplay> get() = _petDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PetDetailsForDisplay()
        )

    init {
        viewModelScope.launch {
            val fetchedPetDetails = getPetDetails()
            if (fetchedPetDetails is GetPetDetailsUseCase.Result.Success) {
                _petDetails.value = fetchedPetDetails.fetchedPet
            }
            _status.value = Status.Loaded(fetchedPetDetails)
        }
    }

    sealed class Status {
        data object Loading: Status()
        data class Loaded(val result: GetPetDetailsUseCase.Result): Status()
    }

    companion object {
        fun provideFactory(getPetDetails: GetPetDetailsUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewPetViewModel::class.java)) {
                    return ViewPetViewModel(getPetDetails) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}