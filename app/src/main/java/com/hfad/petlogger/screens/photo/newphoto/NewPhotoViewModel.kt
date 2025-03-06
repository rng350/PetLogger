package com.hfad.petlogger.screens.photo.newphoto

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.util.GetDateTimeDisplayUseCase
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.domain.MediaRepository
import com.hfad.petlogger.tags.data.Tag
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewPhotoViewModel(private val mediaRepository: MediaRepository) : ViewModel() {
    val photo = MutableLiveData<Photo?>(null)
    val photoTitle = MutableLiveData<String>("")
    val photoFilesizeReadable: String get() = String.format("%.2f", photo.value?.size)
    private val _photoDate: MutableLiveData<String> = MutableLiveData<String>("")
    val photoDate: LiveData<String> get() = _photoDate
    val _goBack: MutableLiveData<Boolean> = MutableLiveData(false)
    val goBack: LiveData<Boolean> get() = _goBack

    fun setPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            val photos = mediaRepository.retrievePhotos(context, listOf<Uri>(uri))
            if (photos.isNotEmpty()) {
                photo.value = photos[0]
                _photoDate.value = GetDateTimeDisplayUseCase().invoke(photo.value?.date)
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

    fun submit(
        newAttachedNotes: List<Note> = listOf<Note>(),
        existingAttachedNotes: List<Note> = listOf<Note>(),
        pets: List<Long> = listOf<Long>(),
        events: List<Long> = listOf<Long>(),
        tags: List<Tag> = listOf<Tag>()
    ) {
        photo.value?.let { photo ->
            viewModelScope.launch {
                async {
                    photoTitle.value?.isNotEmpty()?.let {
                        photo.title = photoTitle.value!!
                    }
                    mediaRepository.insertNewPhoto(
                        photo,
                        pets=pets,
                        existingAttachedNotes = existingAttachedNotes,
                        events=events,
                        tags=tags
                    )
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