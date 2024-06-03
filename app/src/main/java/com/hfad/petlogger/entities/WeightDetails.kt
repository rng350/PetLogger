package com.hfad.petlogger.entities

import androidx.room.Embedded
import androidx.room.Relation

data class WeightDetails(
    @Embedded
    val weight: Weight,

    @Relation(parentColumn = "weight_pet_id", entityColumn = "pet_id")
    val assocPet: Pet
)