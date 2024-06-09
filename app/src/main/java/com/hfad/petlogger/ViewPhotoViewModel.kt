package com.hfad.petlogger

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.repositories.MediaRepository
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.launch
import java.time.ZoneId

class ViewPhotoViewModel(private val mediaRepository: MediaRepository, private val photoId: Long) : ViewModel() {
    val photo: MutableLiveData<Photo> = MutableLiveData<Photo>()

    private val _photoDate: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val photoDate: LiveData<String> get() = _photoDate

    val noteNavigator = Navigator()
    val eventNavigator = Navigator()
    val weightNavigator = Navigator()
    val petNavigator = Navigator()

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