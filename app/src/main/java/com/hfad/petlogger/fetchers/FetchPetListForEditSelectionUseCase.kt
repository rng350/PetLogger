package com.hfad.petlogger.fetchers

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class FetchPetListForEditSelectionUseCase(private val petDao: PetDao) {
    private suspend fun fetchPets(petIdSelection: HashSet<Long>?): List<CheckableItem<PetWithProfilePic>> = withContext(Dispatchers.IO) {
        val allFetchedPets = async {
            petDao.getAllPetsWithProfilePhotos()
        }
        val allPets = allFetchedPets.await()

        val checkablePetList = allPets.map {
            val checked = petIdSelection?.contains(it.pet.petID) ?: false
            CheckableItem<PetWithProfilePic>(it, MutableLiveData(checked))
        }

        checkablePetList
    }
    suspend operator fun invoke(petIdSelection: HashSet<Long>?): List<CheckableItem<PetWithProfilePic>> = withContext(Dispatchers.IO) {
        fetchPets(petIdSelection)
    }

    /*suspend operator fun invoke(petIdSelection: List<Long>): List<CheckableItem<PetWithProfilePic>> = withContext(Dispatchers.IO) {
        fetchPets(petIdSelection.toHashSet())
    }*/

    suspend operator fun invoke(petSelection: List<Pet>): List<CheckableItem<PetWithProfilePic>> = withContext(Dispatchers.IO) {
        val petIdSelection = petSelection.map {
            it.petID
        }
        fetchPets(petIdSelection.toHashSet())
    }

    suspend operator fun invoke(petSelection: Pet): List<CheckableItem<PetWithProfilePic>> = withContext(Dispatchers.IO) {
        fetchPets(hashSetOf(petSelection.petID))
    }
}