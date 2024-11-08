package com.hfad.petlogger.screens.photo.mediaselection

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.photos.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MediaSingleSelectionViewModel(
    private val mediaRepository: MediaRepository,
    private val getInitialPhoto: GetSingleItemUseCase<Photo>? = null
) : ViewModel() {
    private val _initialPhoto = MutableLiveData<Photo>()
    private val _currentPhoto = MutableLiveData<Photo?>()
    val currentPhoto: LiveData<Photo?> get() = _currentPhoto
    private var _photosToAdd = listOf<Photo>()
    val photoToAdd: List<Photo> get() = _photosToAdd
    private var _photoToRemove = listOf<Photo>()
    val photoToRemove: List<Photo> get() = _photoToRemove

    init {
        viewModelScope.launch {
            getInitialPhoto?.invoke()?.let{ photo ->
                _initialPhoto.value = photo
                resetSelection()
            }
        }
    }

    fun resetSelection() {
        _currentPhoto.value = _initialPhoto.value
        _photosToAdd = listOf<Photo>()
        _photoToRemove = listOf<Photo>()
    }

    fun removePhoto() {
        _initialPhoto.value?.let {
            _photoToRemove = listOf<Photo>(it)
        }
        _currentPhoto.value = null
        _photosToAdd = listOf<Photo>()
    }

    fun pickNewPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            val retrievedPhotosDeferred = async {
                mediaRepository.retrievePhotos(context, listOf<Uri>(uri))
            }.await()
            _photosToAdd = retrievedPhotosDeferred
            if (photoToAdd.isNotEmpty()) {
                _currentPhoto.value = photoToAdd[0].copy()
                _initialPhoto.value?.let {
                    _photoToRemove = listOf<Photo>(it)
                }
            }
        }
    }

    companion object {
        fun provideFactory(mediaRepository: MediaRepository, getInitialPhoto: GetSingleItemUseCase<Photo>? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MediaSingleSelectionViewModel::class.java)) {
                    return MediaSingleSelectionViewModel(mediaRepository, getInitialPhoto) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}