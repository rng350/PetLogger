package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.Weight
import com.hfad.guineapiglog.fetchers.Fetcher
import kotlinx.coroutines.launch

class NewWeightViewModel(val weightDao: WeightDao, val petDao: PetDao, petId: Long?) : ViewModel(), WithSinglePetSelection {
    override var petAssociated: MutableLiveData<Pet> = MutableLiveData<Pet>()
    override var pets: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    override var petPicked: MutableLiveData<Int> = MutableLiveData(0)
    val wDateTime = SelectableDateTime()
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
        Log.i("...dateTime:", wDateTime.dateTime.toString() ?: "N/A")
        Log.i("...weight:", weightGrams.value?.toString() ?: "N/A")
        if (petAssociated.value != null && weightGrams.value != null) {
            var weight = Weight()
            weight.weightDateTime = wDateTime.dateTime
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