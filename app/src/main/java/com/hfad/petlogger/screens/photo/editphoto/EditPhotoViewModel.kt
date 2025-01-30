package com.hfad.petlogger.screens.photo.editphoto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.photos.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditPhotoViewModel(private val mediaRepository: MediaRepository, private val photoId: Long) : ViewModel() {
    private val photoInitial = MutableLiveData<Photo>()
    val photoNew = MutableLiveData<Photo>()
    private val _photoDate: MutableLiveData<String> = MutableLiveData<String>("")
    val photoDate: LiveData<String> get() = _photoDate
    private val _goToGalleryList = MutableLiveData(false)
    val goToGalleryList: LiveData<Boolean> get() = _goToGalleryList

    private val _goBack = MutableLiveData(false)
    val goBack: LiveData<Boolean> get() = _goBack

    init {
        viewModelScope.launch {
            photoInitial.value = mediaRepository.getPhoto(photoId)
            photoNew.value = photoInitial.value?.copy()
            _photoDate.value = GetDateTimeDisplayUseCase().invoke(photoInitial.value?.date)
        }
    }

    fun reset() {
        photoNew.value = photoInitial.value?.copy()
    }

    fun submit(petsToAdd: List<Long> = listOf<Long>(),
               petsToRemove: List<Long> = listOf<Long>(),
               eventsToAdd: List<Long> = listOf<Long>(),
               eventsToRemove: List<Long> = listOf<Long>(),
               notesToAdd: List<Note> = listOf<Note>(),
               notesToRemove: List<Note> = listOf<Note>(),
               notesToUpdate: List<Note> = listOf<Note>(),
               tagsToAdd: List<Tag> = listOf<Tag>(),
               tagsToRemove: List<Tag> = listOf<Tag>()
    ) {
        photoNew.value?.let {photo ->
            if (
                    !photo.equals(photoInitial.value)
                    || petsToAdd.isNotEmpty()
                    || petsToRemove.isNotEmpty()
                    || eventsToAdd.isNotEmpty()
                    || eventsToRemove.isNotEmpty()
                    || notesToAdd.isNotEmpty()
                    || notesToRemove.isNotEmpty()
                    || notesToUpdate.isNotEmpty()
                    || tagsToAdd.isNotEmpty()
                    || tagsToRemove.isNotEmpty()
                ) {
                viewModelScope.launch {
                    async {
                        mediaRepository.updatePhoto(
                            photo = photo,
                            petsToAdd = petsToAdd,
                            petsToRemove = petsToRemove,
                            eventsToAdd = eventsToAdd,
                            eventsToRemove = eventsToRemove,
                            notesToAdd = notesToAdd,
                            notesToRemove = notesToRemove,
                            notesToUpdate = notesToUpdate,
                            tagsToAdd = tagsToAdd,
                            tagsToRemove = tagsToRemove
                        )
                    }.await()
                    _goBack.value = true
                }
            } else {
                _goBack.value = true
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