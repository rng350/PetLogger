package com.hfad.petlogger

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.photodisplay.stateless.GetAssociatedItemsUseCase
import com.hfad.petlogger.repositories.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MediaSelectionViewModel(private val mediaRepository: MediaRepository,
                              private val fetchInitialSelection: GetAssociatedItemsUseCase<Photo>? = null,
                              val maxItems: Int = Int.MAX_VALUE) : ViewModel() {
    private var initialSelection = HashSet<Photo>()
    private var _currentPhotoSelection = MutableLiveData<List<Photo>>(listOf<Photo>())
    val currentPhotoSelection: LiveData<List<Photo>> get() = _currentPhotoSelection
    private var _selectionToRemove = MutableLiveData<List<Photo>>(listOf<Photo>())
    val selectionToRemove: LiveData<List<Photo>> get() = _selectionToRemove
    private var _selectionToAdd = MutableLiveData<List<Photo>>(listOf<Photo>())
    val selectionToAdd: LiveData<List<Photo>> get() = _selectionToAdd

    init {
        viewModelScope.launch {
            async {
                fetchInitialSelection?.let { getInitialPhotos ->
                    val initialPhotos = getInitialPhotos()
                    initialSelection.addAll(initialPhotos)
                }
            }.await()
            resetSelection()
        }
    }

    fun resetSelection() {
        _currentPhotoSelection.value = initialSelection.toList()
        _selectionToAdd.value = listOf<Photo>()
        _selectionToRemove.value = listOf<Photo>()
    }

    fun retrievePhotoSelectionFromPickerResults(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            val newAddedSelection = mediaRepository.retrievePhotos(context, uris)
            // hashsets used to prevent duplicate additions
            val oldAddedSelection = _selectionToAdd.value?.toHashSet() ?: HashSet<Photo>()
            oldAddedSelection.addAll(newAddedSelection)
            val newSelectionToAdd = oldAddedSelection.toList()
            _selectionToAdd.value = newSelectionToAdd
            val currentSelectionMutable = currentPhotoSelection.value?.toHashSet() ?: HashSet<Photo>()
            currentSelectionMutable.addAll(_selectionToAdd.value ?: listOf())
            _currentPhotoSelection.value = currentSelectionMutable.toList()
        }
    }

    fun removePhotoFromSelection(photo: Photo) {
        val curSelectionMutable = _currentPhotoSelection.value?.toMutableList() ?: mutableListOf<Photo>()
        curSelectionMutable.remove(photo)
        _currentPhotoSelection.value = curSelectionMutable.toList()
        if (initialSelection.contains(photo)) {
            val selectionToRemoveMutable = _selectionToRemove.value?.toMutableList() ?: mutableListOf<Photo>()
            selectionToRemoveMutable.add(photo)
            _selectionToRemove.value = selectionToRemoveMutable.toList()
        } else {
            val selectionToAddMutable = _selectionToAdd.value?.toMutableList() ?: mutableListOf<Photo>()
            selectionToAddMutable.remove(photo)
            _selectionToAdd.value = selectionToAddMutable.toList()
        }
    }

    fun getPhotosToAdd(): List<Photo> {
        return selectionToAdd.value ?: listOf<Photo>()
    }

    fun getPhotosToRemove(): List<Photo> {
        return selectionToRemove.value ?: listOf<Photo>()
    }
    companion object {
        fun provideFactory(mediaRepository: MediaRepository, fetchInitialSelection: GetAssociatedItemsUseCase<Photo>? = null, maxItems: Int = Int.MAX_VALUE): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MediaSelectionViewModel::class.java)) {
                    return MediaSelectionViewModel(mediaRepository, fetchInitialSelection, maxItems) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}