package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.OffsetDateTime
import java.time.Period

class ViewPetViewModel (val petDao: PetDao, val weightDao: WeightDao, val petID: Long): ViewModel() {
    var pet : MutableLiveData<Pet> = MutableLiveData<Pet>()
    /*var petDOB : MutableLiveData<String> = MutableLiveData<String>("N/A")*/
    val petAge : MutableLiveData<String> = MutableLiveData<String>("N/A")
    val eventsAssociated: MutableLiveData<MutableList<Event>> = MutableLiveData(mutableListOf<Event>())
    val weightsAssociated: MutableLiveData<MutableList<Weight>> = MutableLiveData(mutableListOf<Weight>())
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()

    init {
        fetchPet()
        Fetcher.fetchWeightsOfPet(this, weightsAssociated, weightDao, petID)
        Fetcher.fetchEventsOfPet(this, eventsAssociated, petDao, petID)
    }

/*    fun getBirthDateDisplay(): String {
        pet.value?.let {
            if (it.hasDOB) {
                // return DateFormat.getDateInstance(DateFormat.MEDIUM).parse(it.petDOB.toString()).toString()
                val day = it.petDOB.dayOfMonth
                val month = it.petDOB.monthValue
                val year = it.petDOB.year
                return "${day}/${month}/${year}"
            }
        }
        Log.i("VIEWPET", "no dob detected")
        if (pet.value == null)
            Log.i("VIEWPET", "pet is null???")
        return "N/A"
    }*/

    fun getPetAgeDisplay(): String {
        pet.value?.let {
            if (it.hasDOB) {
                val period = Period.between(it.petDOB.toLocalDate(), OffsetDateTime.now().toLocalDate())
                Log.i("VIEWPET", "pet has age... ${period.years} years, ${period.months} months, ${period.days} days")
                return ("${period.years} years, ${period.months} months, ${period.days} days")
            }
        }
        Log.i("VIEWPET", "no age detected")
        if (pet.value == null)
            Log.i("VIEWPET", "pet is null???")
        return "N/A"
    }

    private fun fetchPet() {
        viewModelScope.launch {
            var fetchedPet = async { petDao.getAsync(petID) }

            pet.value = fetchedPet.await()
            /*petDOB.value = getBirthDateDisplay()*/
            petAge.value = getPetAgeDisplay()
        }
    }

    fun editPet() {
    }

    // 1. click on pencil
    // 2. events vibrate, instructions say "swipe event away to delete"
    fun editEvents() {
    }

    fun editWeights() {
    }

    fun addEvent() {
        TODO("new event screen, with this pet checked as associated pet")
    }

    fun addWeight() {
    }

    fun removeEvent(eventID: Long) {
    }

    fun removeWeight(weightID: Long) {
    }
}