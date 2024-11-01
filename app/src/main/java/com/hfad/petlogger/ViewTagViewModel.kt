package com.hfad.petlogger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.EventForList
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.PetWithProfilePic
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.entities.WeightForList
import com.hfad.petlogger.repositories.TagRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewTagViewModel(tagRepository: TagRepository, tagId: Long) : ViewModel() {
    private val _tag: MutableLiveData<Tag> = MutableLiveData()
    val tag: LiveData<Tag> get() = _tag

    val taggedPets: StateFlow<List<PetWithProfilePic>> =
        tagRepository
            .getPetsOfTagAsFlow(tagId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
    val taggedEvents: StateFlow<List<EventForList>> =
        tagRepository
            .getEventsOfTagAsFlow(tagId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
    val taggedNotes: StateFlow<List<Note>> =
        tagRepository
            .getNotesOfTagAsFlow(tagId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
    val taggedWeights: StateFlow<List<WeightForList>> =
        tagRepository
            .getWeightsOfTagAsFlow(tagId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
    val taggedPhotos: StateFlow<List<Photo>> =
        tagRepository
            .getPhotosOfTagAsFlow(tagId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )
    val petNavigator = Navigator()
    val weightNavigator = Navigator()
    val eventNavigator = Navigator()
    val noteNavigator = Navigator()
    val photoNavigator = Navigator()

    init {
        viewModelScope.launch {
            _tag.value = tagRepository.getTag(tagId)
        }
    }
    companion object {
        fun provideFactory(tagRepository: TagRepository, tagId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewTagViewModel::class.java)) {
                    return ViewTagViewModel(tagRepository, tagId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}