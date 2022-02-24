package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.text.DateFormat
import java.time.OffsetDateTime
import java.time.Period

class ViewPetViewModel (val petDao: PetDao, val petID: Long): ViewModel() {
    val pet : LiveData<Pet> = petDao.get(petID)
    val petDOB : String = getBirthDateDisplay()
    val petAge : String = getPetAgeDisplay()

    private fun getBirthDateDisplay(): String {
        pet.value?.let {
            if (it.hasDOB) {
                return DateFormat.getDateInstance(DateFormat.MEDIUM).parse(it.petDOB.toString()).toString()
            }
        }
        return "N/A"
    }

    private fun getPetAgeDisplay(): String {
        pet.value?.let {
            if (it.hasDOB) {
                val period = Period.between(OffsetDateTime.now().toLocalDate(), it.petDOB.toLocalDate())
                return ("${period.years} years, ${period.months} months, ${period.days} days")
            }
        }
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