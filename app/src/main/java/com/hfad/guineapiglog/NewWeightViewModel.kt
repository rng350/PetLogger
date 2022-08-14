package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class NewWeightViewModel(val weightDao: WeightDao, val petDao: PetDao, petId: Long?) : ViewModel(), WithSinglePetSelection, WithDateTime {
    override var petAssociated: MutableLiveData<Pet> = MutableLiveData<Pet>()
    override var pets: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    override var petPicked: MutableLiveData<Int> = MutableLiveData(0)
    override var dateTime: MutableLiveData<OffsetDateTime> = MutableLiveData(OffsetDateTime.now())
    override var dateDisplay: MutableLiveData<String> = MutableLiveData(dateTime.value?.toLocalDate().toString())
    override var timeDisplay: MutableLiveData<String> = MutableLiveData(dateTime.value?.toLocalTime().toString())
    var petNameDisplay: MutableLiveData<String> = MutableLiveData<String>()
    var weightGrams: MutableLiveData<Int> = MutableLiveData<Int>()
    var details: MutableLiveData<String> = MutableLiveData<String>()

    init {
        petId?.let {
            Fetcher.fetchPet(this, petAssociated, petDao, it)
            petNameDisplay.value = petAssociated.value?.petName
        }
        Fetcher.fetchAllPets(this, pets, petDao)
    }

    fun submitWeight() {
        Log.i("weighted_Pet:", petAssociated.value?.toString() ?: "N/A")
        Log.i("...dateTime:", dateTime.value?.toString() ?: "N/A")
        Log.i("...weight:", weightGrams.value?.toString() ?: "N/A")
        if (petAssociated.value != null && dateTime.value != null && weightGrams.value != null) {
            var weight = Weight()
            weight.weightDateTime = dateTime.value!!
            weight.petId = petAssociated.value!!.petID
            weight.weightGrams = weightGrams.value!!
            details.value?.let {
                weight.weightNotes = it
            }
            viewModelScope.launch {
                weightDao.insert(weight)
                Log.i("WEIGHT:", weight.toString())
            }
        }
    }
}