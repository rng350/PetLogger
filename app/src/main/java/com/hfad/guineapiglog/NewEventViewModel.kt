package com.hfad.guineapiglog

import android.util.Log
import androidx.lifecycle.*
import java.time.OffsetDateTime
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewEventViewModel(val eventDao: EventDao, val eventPetDao: EventPetDao, val petDao: PetDao): ViewModel(), WithMultiPetSelection {
    var eventTitle: String = "N/A"
    var eventDetails: String = "N/A"
    var eventDateTime = SelectableDateTime()
    override var pets : MutableList<Pet>? = null
    override var petsAssociated: MutableLiveData<MutableList<Pet>> = MutableLiveData(mutableListOf<Pet>())
    override lateinit var petsPicked: MutableLiveData<BooleanArray>
    private val _eventID = MutableLiveData<Long>(null)
    val eventID: LiveData<Long>
        get() = _eventID

    init {
        fetchPets()
        Log.e("associatedID", "address from NewEventViewModel: ${eventID}")
    }

    fun addEvent() {
        viewModelScope.launch {
            var event = Event(date = eventDateTime.dateTime)
            event.title = eventTitle
            event.details = eventDetails
            event.date = eventDateTime.dateTime
            val eventInsert = async {
                _eventID.value = eventDao.insert(event)
                Log.d("associated_id", "event id out! id: ${eventID.value ?: "nil"}")
                Log.d("associated_id", "has observers: ${eventID.hasObservers()}")
                Log.d("associated_id", "has active observers: ${eventID.hasActiveObservers()}")
            }
            eventInsert.await().let {
                eventID.value?.let {
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
                petDao.getAll()
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