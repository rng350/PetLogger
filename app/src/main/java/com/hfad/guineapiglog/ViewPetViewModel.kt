package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.OffsetDateTime
import java.time.Period

class ViewPetViewModel (val petDao: PetDao, val petID: Long): ViewModel() {
    val pet : LiveData<Pet> = petDao.get(petID)
    var petDOB : MutableLiveData<String> = MutableLiveData<String>(getPetAgeDisplay())
    var petAge : MutableLiveData<String> = MutableLiveData<String>(getPetAgeDisplay())

    fun getBirthDateDisplay(): String {
        pet.value?.let {
            if (it.hasDOB) {
                // return DateFormat.getDateInstance(DateFormat.MEDIUM).parse(it.petDOB.toString()).toString()
                val day = it.petDOB.dayOfMonth
                val month = it.petDOB.month
                val year = it.petDOB.year
                return "${day}/${month}/${year}"
            }
        }
        Log.i("VIEWPET", "no dob detected")
        if (pet.value == null)
            Log.i("VIEWPET", "pet is null???")
        return "N/A"
    }

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

    fun editPet() {
    }

    fun viewEvent() {

    }

    fun viewWeight() {

    }

    // 1. click on pencil
    // 2. events vibrate, instructions say "swipe event away to delete"
    fun editEvents() {
    }

    fun editWeights() {
    }

    fun addEvent() {
    }

    fun addWeight() {
    }

    fun removeEvent() {
    }

    fun removeWeight() {
    }
}