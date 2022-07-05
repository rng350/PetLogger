package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData

interface WithSinglePetSelection {
    var pets : MutableLiveData<MutableList<Pet>>
    var petAssociated: MutableLiveData<Pet>
    var petPicked: MutableLiveData<Int>
}