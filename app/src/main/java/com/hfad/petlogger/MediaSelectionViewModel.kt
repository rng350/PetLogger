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
            val newAddedSelectionFetched = async {
                mediaRepository.retrievePhotos(context, uris)
            }

            // Step #1: populate hashmap with cur selection
            val currentPhotoSelectionHashMap = HashMap<Long, Photo>()
            currentPhotoSelection.value?.let {currentPhotos ->
                currentPhotos.map { photo->
                    currentPhotoSelectionHashMap.put(photo.id, photo)
                }
            }
            val currentPhotoSelectionToRemoveHashMap = HashMap<Long, Photo>()
            selectionToRemove.value?.let {currentSelectionToRemove ->
                currentSelectionToRemove.map {photo ->
                    currentPhotoSelectionToRemoveHashMap.put(photo.id, photo)
                }
            }

            // Step #2: Check for photo selection overlap
            val newAddedSelection = newAddedSelectionFetched.await().toMutableList()
            val newSelectionToAdd = selectionToAdd.value?.toMutableList() ?: mutableListOf()
            val newSelectionToRemove = selectionToRemove.value?.toMutableList() ?: mutableListOf()

            // checks to prevent unnecessary observer notifications and refreshing
            var selectionToAddHasChanged = false
            var selectionToRemoveHasChanged = false
            var currentSelectionHasChanged = false

            newAddedSelection.map { photo ->
                // Check against selection to remove first...
                if (currentPhotoSelectionToRemoveHashMap.contains(photo.id)) {
                    currentPhotoSelectionToRemoveHashMap[photo.id]?.let {photoToAddBackIn ->
                        currentPhotoSelectionHashMap[photo.id] = photoToAddBackIn
                        newSelectionToRemove.remove(photoToAddBackIn)

                        selectionToRemoveHasChanged = true
                        currentSelectionHasChanged = true
                    }
                }
                //  Check that retrieved photos don't already exist in selection
                if (!currentPhotoSelectionToRemoveHashMap.contains(photo.id) && !currentPhotoSelectionHashMap.contains(photo.id)) {
                    currentPhotoSelectionHashMap[photo.id] = photo
                    newSelectionToAdd.add(photo)

                    selectionToAddHasChanged = true
                    currentSelectionHasChanged = true
                }
            }

            if (selectionToRemoveHasChanged) _selectionToRemove.value = newSelectionToRemove
            if (selectionToAddHasChanged) _selectionToAdd.value = newSelectionToAdd
            if (currentSelectionHasChanged) _currentPhotoSelection.value = currentPhotoSelectionHashMap.map { mapEntry -> mapEntry.value }
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