package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.entities.Pet

interface WithMultiPetSelection {
    var pets : MutableList<Pet>?
    var petsAssociated: MutableLiveData<List<Pet>>
    var petsPicked: MutableLiveData<BooleanArray>
}