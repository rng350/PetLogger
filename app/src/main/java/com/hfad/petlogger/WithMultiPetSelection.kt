package com.hfad.petlogger

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.entities.Pet

interface WithMultiPetSelection {
    var pets : MutableList<Pet>?
    var petsAssociated: MutableLiveData<List<Pet>>
    var petsPicked: MutableLiveData<BooleanArray>
}