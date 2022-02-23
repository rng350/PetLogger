package com.hfad.guineapiglog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class HomeViewModel(val petDao: PetDao, val eventDao: EventDao) : ViewModel() {
    val pets = petDao.getAll()
    private val _navigateToPet = MutableLiveData<Long?>()
    val navigateToPet: LiveData<Long?>
        get() = _navigateToPet

    val events = eventDao.getAll()
    private val _navigateToEvent = MutableLiveData<Long?>()
    val navigateToEvent: LiveData<Long?>
        get() = _navigateToEvent

    fun deletePet(pet: Pet) {
        viewModelScope.launch {
            petDao.delete(pet)
        }
    }

    fun deleteEvent() {
        // TODO: implement
    }
}