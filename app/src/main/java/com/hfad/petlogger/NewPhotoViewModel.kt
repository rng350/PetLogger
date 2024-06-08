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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewPhotoViewModel(private val mediaRepository: MediaRepository) : ViewModel() {
    // TODO: Implement the ViewModel
    val photo = MutableLiveData<Photo?>(null)
    val photoTitle = MutableLiveData<String>("")
    val photoFilesizeReadable: String get() = String.format("%.2f", photo.value?.size)
    //val photoDate: String get() = photo.value?.date?.toString() ?: "N/A"
    private val _photoDate: MutableLiveData<String> = MutableLiveData<String>("N/A")
    val photoDate: LiveData<String> get() = _photoDate
    val _goBack: MutableLiveData<Boolean> = MutableLiveData(false)
    val goBack: LiveData<Boolean> get() = _goBack

    fun setPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            val photos = mediaRepository.retrievePhotos(context, listOf<Uri>(uri))
            if (photos.isNotEmpty()) {
                photo.value = photos[0]
                _photoDate.value = photo.value?.date?.toString() ?: "N/A"
            }
        }
    }

    fun resetPhotoSelection() {
        photo.value = null
        photoTitle.value = ""
        _photoDate.value = "N/A"
    }

    fun fullPhotoDetailsReset(
        resetNoteSelection: ()->Unit,
        resetWeightSelection: ()->Unit,
        resetEventSelection: ()->Unit,
        resetPetSelection: ()->Unit
    ) {
        resetPhotoSelection()
        resetEventSelection()
        resetNoteSelection()
        resetPetSelection()
        resetWeightSelection()
    }

    fun submit() {
        photo.value?.let {
            viewModelScope.launch {
                async {
                    mediaRepository.insertNewPhoto(it)
                }.await()
                resetPhotoSelection()
                _goBack.value = true
            }
        }
    }

    companion object {
        fun provideFactory(mediaRepository: MediaRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NewPhotoViewModel::class.java)) {
                    return NewPhotoViewModel(mediaRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}