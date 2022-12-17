package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.entities.Pet

interface WithSinglePetSelection {
    var pets : MutableLiveData<MutableList<Pet>>
    var petAssociated: MutableLiveData<Pet>
    var petPicked: MutableLiveData<Int>
}