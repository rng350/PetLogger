package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import com.hfad.guineapiglog.entities.Pet

interface WithSinglePetSelection {
    var pets : MutableLiveData<List<Pet>>
    var petAssociated: MutableLiveData<Pet>
    var petPicked: MutableLiveData<Int>
}