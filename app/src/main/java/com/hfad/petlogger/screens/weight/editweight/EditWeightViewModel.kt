package com.hfad.petlogger.screens.weight.editweight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.SelectableDateTime
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight
import com.hfad.petlogger.weights.WeightRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditWeightViewModel(private val weightRepository: WeightRepository, private val weightId: Long): ViewModel() {
    val weight = MutableLiveData<Weight>()
    val weightDateTime = SelectableDateTime()
    private val _goToWeightsList = MutableLiveData(false)
    val goToWeightsList: LiveData<Boolean> get() = _goToWeightsList
    private val _goToViewWeight = MutableLiveData(false)
    val goToViewWeight: LiveData<Boolean> get() = _goToViewWeight
    init {
        viewModelScope.launch {
            val fetchedWeightDetails = async {
                weightRepository.getWeightDetails(weightId)
            }
            val weightDetails = fetchedWeightDetails.await()
            weight.value = weightDetails.weight
            weight.value?.let {
                weightDateTime.set(it.weightDateTime)
            }
        }
    }

    fun submitChanges(
        petId: Long,
        notesToAdd: List<Note> = listOf<Note>(),
        notesToRemove: List<Note> = listOf<Note>(),
        notesToUpdate: List<Note> = listOf<Note>(),
        tagsToAdd: List<Tag> = listOf<Tag>(),
        tagsToRemove: List<Tag> = listOf<Tag>()
    ) {
        weight.value?.let {
            viewModelScope.launch {
                weightRepository.update(
                    weight = Weight(it.id, petId, it.weightGrams, weightDateTime.selectedDateTime, it.weightNotes),
                    notesToAdd = notesToAdd,
                    notesToRemove = notesToRemove,
                    notesToUpdate = notesToUpdate,
                    tagsToAdd = tagsToAdd,
                    tagsToRemove = tagsToRemove
                )
                _goToViewWeight.value = true
            }
        }
    }

    fun deleteWeight() {
        weight.value?.let {
            viewModelScope.launch {
                async {
                    weightRepository.delete(it)
                }.await()
                _goToWeightsList.value = true
            }
        }
    }

    fun onNavigateToWeightsList() {
        _goToWeightsList.value = false
    }

    fun onNavigateToViewWeight() {
        _goToViewWeight.value = false
    }

    companion object {
        fun provideFactory(weightRepository: WeightRepository, weightId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditWeightViewModel::class.java)) {
                    return EditWeightViewModel(weightRepository, weightId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}