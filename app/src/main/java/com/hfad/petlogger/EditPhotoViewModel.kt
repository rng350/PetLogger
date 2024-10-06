package com.hfad.petlogger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Event
import com.hfad.petlogger.entities.Note
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.entities.Weight
import com.hfad.petlogger.repositories.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditPhotoViewModel(private val mediaRepository: MediaRepository, private val photoId: Long) : ViewModel() {
    // TODO: Implement the ViewModel
    private val photoInitial = MutableLiveData<Photo>()
    val photoNew = MutableLiveData<Photo>()
    private val _photoDate: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val photoDate: LiveData<String> get() = _photoDate
    private val _goToGalleryList = MutableLiveData(false)
    val goToGalleryList: LiveData<Boolean> get() = _goToGalleryList

    private val _goBack = MutableLiveData(false)
    val goBack: LiveData<Boolean> get() = _goBack

    init {
        viewModelScope.launch {
            photoInitial.value = mediaRepository.getPhoto(photoId)
            photoNew.value = photoInitial.value?.copy()
        }
    }

    fun reset() {
        photoNew.value = photoInitial.value?.copy()
    }

    fun submit(petsToAdd: List<Long> = listOf<Long>(),
               petsToRemove: List<Long> = listOf<Long>(),
               eventsToAdd: List<Event> = listOf<Event>(),
               eventsToRemove: List<Event> = listOf<Event>(),
               weightsToAdd: List<Weight> = listOf<Weight>(),
               weightsToRemove: List<Weight> = listOf<Weight>(),
               notesToAdd: List<Note> = listOf<Note>(),
               notesToRemove: List<Note> = listOf<Note>(),
               notesToUpdate: List<Note> = listOf<Note>()) {
        photoNew.value?.let {photo ->
            if (!photo.equals(photoInitial.value)) {
                viewModelScope.launch {
                    async {
                        mediaRepository.updatePhoto(
                            photo = photo,
                            petsToAdd = petsToAdd,
                            petsToRemove = petsToRemove,
                            eventsToAdd = eventsToAdd,
                            eventsToRemove = eventsToRemove,
                            weightsToAdd = weightsToAdd,
                            weightsToRemove = weightsToRemove,
                            notesToAdd = notesToAdd,
                            notesToRemove = notesToRemove,
                            notesToUpdate = notesToUpdate
                        )
                    }.await()
                    _goBack.value = true
                }
            }
        }
    }

    fun deletePhoto() {
        viewModelScope.launch {
            photoInitial.value?.let {
                viewModelScope.launch {
                    async {
                        mediaRepository.delete(it)
                    }.await()
                    _goToGalleryList.value = true
                }
            }
        }
    }

    fun onNavigateToGalleryList() {
        _goToGalleryList.value = false
    }

    companion object {
        fun provideFactory(mediaRepository: MediaRepository, photoId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditPhotoViewModel::class.java)) {
                    return EditPhotoViewModel(mediaRepository, photoId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}