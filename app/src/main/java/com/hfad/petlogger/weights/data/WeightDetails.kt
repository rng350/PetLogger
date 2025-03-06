package com.hfad.petlogger.weights.data

import androidx.room.Embedded
import androidx.room.Relation
import com.hfad.petlogger.pets.data.Pet

data class WeightDetails(
    @Embedded
    val weight: Weight,

    @Relation(parentColumn = "weight_pet_id", entityColumn = "pet_id")
    val assocPet: Pet
)