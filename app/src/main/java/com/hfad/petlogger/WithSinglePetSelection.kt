package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.entities.Pet

interface WithSinglePetSelection {
    var pets : MutableLiveData<List<Pet>>
    var petAssociated: MutableLiveData<Pet>
    var petPicked: MutableLiveData<Int>
}