package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.OffsetDateTime
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewEventViewModel(val eventDao: EventDao, val eventPetDao: EventPetDao, val petDao: PetDao): ViewModel(), WithMultiPetSelection {
    var eventTitle: String = "N/A"
    var eventDetails: String = "N/A"
    var eventDateTime: OffsetDateTime = OffsetDateTime.now()
    var eventDateDisplay: String = "Event date"
    var eventTimeDisplay: String = "Event time"
    override var pets : MutableList<Pet>? = null
    override var petsAssociated: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    override lateinit var petsPicked: MutableLiveData<BooleanArray>

    init {
        fetchPets()
    }

    fun addEvent() {
        viewModelScope.launch {
            var event = Event(date = eventDateTime)
            event.title = eventTitle
            event.details = eventDetails
            event.date = eventDateTime
            var eventID: Long? = null
            val eventInsert = async {
                eventID = eventDao.insert(event)
            }
            eventInsert.await().let {
                eventID?.let {
                    if ((petsAssociated.value?.size ?: 0) > 0) {
                        for (pet in petsAssociated.value!!) {
                            Log.e("PET EVENT INSERT", "pet being added: ${pet.toString()}")
                            Log.e("PET EVENT INSERT", "event id: $it")
                            eventPetDao.insert(EventPet(eventId = it, petId = pet.petID))
                        }
                    }
                }
            }
        }
    }

    private fun fetchPets() {
        viewModelScope.launch {
            var fetchedPets = async {
                petDao.getAllAsync()
            }
            pets = fetchedPets.await()
            petsPicked = MutableLiveData(BooleanArray(pets?.size ?: 0))
        }
    }

    fun removeAssociatedPet(pet: Pet) {
        petsAssociated.value?.remove(pet)
        pets?.let {
            val index = it.indexOf(pet)
            petsPicked.value!![index] = false
        }
    }
}