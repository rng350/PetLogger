package com.hfad.petlogger.screens.photo.viewphoto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.photos.MediaRepository
import kotlinx.coroutines.launch
import java.time.ZoneId

class ViewPhotoViewModel(private val mediaRepository: MediaRepository, private val photoId: Long) : ViewModel() {
    val photo: MutableLiveData<Photo> = MutableLiveData<Photo>()

    private val _photoDate: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val photoDate: LiveData<String> get() = _photoDate

    init {
        viewModelScope.launch {
            photo.value = mediaRepository.getPhoto(photoId)
            _photoDate.value =
                photo.
                value?.
                date?.
                atZoneSameInstant(ZoneId.systemDefault())?.
                toLocalDateTime().
                toString()
                    ?: "N/A"
        }
    }
    companion object {
        fun provideFactory(mediaRepository: MediaRepository, photoId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewPhotoViewModel::class.java)) {
                    return ViewPhotoViewModel(mediaRepository, photoId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}