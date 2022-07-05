package com.hfad.guineapiglog

import androidx.lifecycle.ViewModel
import java.time.OffsetDateTime

class WeightWithPetName(val weight: Weight, petDao: PetDao, viewModel: ViewModel) {
    var weightId: Long = weight.id
    var weightGrams: Int = weight.weightGrams
    var weightDateTime : OffsetDateTime = weight.weightDateTime
    var petName: String = Fetcher.fetchPet(viewModel, petDao, weight.petId)?.petName ?: "N/A"
}
