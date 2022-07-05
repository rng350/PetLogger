package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

interface WithMultiPetSelection {
    var pets : MutableList<Pet>?
    var petsAssociated: MutableLiveData<MutableList<Pet>>
    var petsPicked: MutableLiveData<BooleanArray>
}