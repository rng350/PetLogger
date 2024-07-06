package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.*
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.fetchers.Fetcher
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.repositories.PetRepository
import com.hfad.petlogger.util.GetDateDisplayUseCase
import com.hfad.petlogger.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.util.GetPeriodDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.Period

class ViewPetViewModel (
    private val petRepository: PetRepository,
    private val petID: Long,
    private val getPetAgeDisplay: GetPeriodDisplayUseCase,
    private val notAvailableString: String
): ViewModel() {
    val pet : MutableLiveData<Pet> = MutableLiveData<Pet>()
    val petProfilePhoto = MutableLiveData<Photo>()
    val petBirthdate : MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val petAge : MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val mostRecentWeightAmtDisplay: MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val mostRecentWeightDateDisplay: MutableLiveData<String> = MutableLiveData<String>(notAvailableString)
    val getDateTime = GetDateTimeDisplayUseCase()
    val getDate = GetDateDisplayUseCase()

    init {
        viewModelScope.launch {
            pet.value = async {
                petRepository.getPet(petID)
            }.await()
            pet.value?.let {
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