package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.*
import com.hfad.guineapiglog.dao.PetDao
import com.hfad.guineapiglog.dao.WeightDao
import com.hfad.guineapiglog.entities.Event
import com.hfad.guineapiglog.entities.Pet
import com.hfad.guineapiglog.entities.Photo
import com.hfad.guineapiglog.entities.Weight
import com.hfad.guineapiglog.fetchers.Fetcher
import com.hfad.guineapiglog.util.Navigator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.Period

class ViewPetViewModel (val petDao: PetDao, val weightDao: WeightDao, val petID: Long): ViewModel() {
    val pet : MutableLiveData<Pet> = MutableLiveData<Pet>()
    val petProfilePhoto = MutableLiveData<Photo>()
    /*var petDOB : MutableLiveData<String> = MutableLiveData<String>("N/A")*/
    val petAge : MutableLiveData<String> = MutableLiveData<String>("N/A")
    val eventsAssociated: MutableLiveData<MutableList<Event>> = MutableLiveData(mutableListOf<Event>())
    val weightsAssociated: MutableLiveData<MutableList<Weight>> = MutableLiveData(mutableListOf<Weight>())
    val mostRecentWeightAmtDisplay: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val mostRecentWeightDateDisplay: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()

    init {
        fetchPet()
        fetchWeights()
        Fetcher.fetchEventsOfPet(this, eventsAssociated, petDao, petID)
        Fetcher.fetchPetProfilePhoto(viewModelScope, petProfilePhoto, petDao, petID)
        //Fetcher.fetchWeightsOfPet(viewModelScope, weightsAssociated, petDao, petID)
    }

    private fun getPetAgeDisplay(): String {
        pet.value?.let {
            if (it.hasDOB) {
                val period = Period.between(it.petDOB.toLocalDate(), OffsetDateTime.now().toLocalDate())
                if (period.years > 0)
                    return "${period.years} ${getYearsLabel(period.years)}, " +
                            "${period.months} ${getMonthsLabel(period.months)}, " +
                            "${period.days} ${getDaysLabel(period.days)}"
                else if (period.months > 0)
                    return "${period.months} ${getMonthsLabel(period.months)}, " +
                            "${period.days} ${getDaysLabel(period.days)}"
                else return "${period.days} ${getDaysLabel(period.days)}"
            }
        }
        Log.i("VIEWPET", "no age detected")
        if (pet.value == null)
            Log.i("VIEWPET", "pet is null???")
        return "N/A"
    }

    private fun getYearsLabel(years: Int): String {
        return if (years == 1) "year" else "years"
    }
    private fun getMonthsLabel(months: Int): String {
        return if (months == 1) "month" else "months"
    }
    private fun getDaysLabel(days: Int): String {
        return if (days == 1) "day" else "days"
    }

    private fun fetchPet() {
        viewModelScope.launch {
            val fetchedPet = async { petDao.getAsync(petID) }
            pet.value = fetchedPet.await()
            /*petDOB.value = getBirthDateDisplay()*/
            petAge.value = getPetAgeDisplay()
        }
    }

    private fun fetchWeights() {
        viewModelScope.launch {
            val fetchedWeights = async {petDao.getWeightsOfPet(petID)}
            weightsAssociated.value = fetchedWeights.await()
                .sortedByDescending { it.weightDateTime }
                .toMutableList()
            weightsAssociated.value?.let {
                if (it.size > 0) {
                    mostRecentWeightAmtDisplay.value = "${it[0].weightGrams} grams"
                    mostRecentWeightDateDisplay.value = it[0].weightDateTime.toLocalDate().toString()
                }
            }
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