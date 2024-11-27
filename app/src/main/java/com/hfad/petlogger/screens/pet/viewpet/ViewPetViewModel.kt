package com.hfad.petlogger.screens.pet.viewpet

import androidx.lifecycle.*
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.pets.PetRepository
import com.hfad.petlogger.common.util.GetDateDisplayUseCase
import com.hfad.petlogger.common.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.common.util.GetPeriodDisplayUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ViewPetViewModel (
    private val petRepository: PetRepository,
    private val petID: Long,
    private val getPetAgeDisplay: GetPeriodDisplayUseCase,
    notAvailableString: String
): ViewModel() {
    private val _pet : MutableLiveData<Pet> = MutableLiveData<Pet>()
    val pet: LiveData<Pet> get() = _pet
    val petProfilePhoto = MutableLiveData<Photo>()
    val petBirthdate : MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val petAge : MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val mostRecentWeightAmtDisplay: MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val mostRecentWeightDateDisplay: MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val getDateTime = GetDateTimeDisplayUseCase()
    val getDate = GetDateDisplayUseCase()

    init {
        viewModelScope.launch {
            _pet.value = async {
                petRepository.getPet(petID)
            }.await()
            _pet.value?.let {
                it.petDOB?.let {
                    petBirthdate.value = getDate(it)
                    petAge.value = getPetAgeDisplay(it)
                }
            }
        }
        viewModelScope.launch {
            val fetchedPetProfilePhoto = petRepository.getPetProfilePhoto(petID)
            fetchedPetProfilePhoto?.let {
                petProfilePhoto.value = it
            }
        }
    }

    fun setLatestWeight(weights: List<Weight>, gramSingular: String = "gram", gramPlural: String = "grams") {
        if (weights.isNotEmpty()) {
            mostRecentWeightAmtDisplay.value = "${weights[0].weightGrams} ${if (weights[0].weightGrams == 1) gramSingular else gramPlural}"
            mostRecentWeightDateDisplay.value = getDateTime(weights[0].weightDateTime)
        }
    }

    companion object {
        fun provideFactory(petRepository: PetRepository, petID: Long, getPetAgeDisplay: GetPeriodDisplayUseCase, notAvailableString: String = "N/A"): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewPetViewModel::class.java)) {
                    return ViewPetViewModel(petRepository, petID, getPetAgeDisplay, notAvailableString) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}