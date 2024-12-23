package com.hfad.petlogger.screens.photo.mediaselection

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.selectiontracker.CheckPhotoIsInCurrentSelectionUseCase
import com.hfad.petlogger.common.selectiontracker.CheckPhotoIsInSelectionToRemoveUseCase
import com.hfad.petlogger.common.selectiontracker.MediaMultiSelectionDisplay
import com.hfad.petlogger.common.selectiontracker.MediaMultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.photos.MediaRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaSelectionViewModel(
    private val mediaRepository: MediaRepository,
    val maxItems: Int = Int.MAX_VALUE,
    getInitialSelection: GetMultipleInitialItemsUseCase<Photo>? = null,
    getAssociatedItems: GetItemsUseCase<Photo>? = null,
    getSearchedPhotos: GetSearchedItemsUseCase<Photo>? = null
) : ViewModel() {
    private val mediaSelectionDisplay: MediaMultiSelectionDisplay<Photo> =
        if (getAssociatedItems!=null && getSearchedPhotos!=null) {
            MediaMultiSelectionDisplay<Photo> (
                getInitialSelection = getInitialSelection,
                getSearchedItems = getSearchedPhotos,
                getAssociatedItemsPaginated = getAssociatedItems,
                coroutineScope = viewModelScope,
                checkItemIsInToAddList = CheckPhotoIsInCurrentSelectionUseCase(),
                checkItemIsInToRemoveList = CheckPhotoIsInSelectionToRemoveUseCase(),
                checkItemIsInToKeepList = CheckPhotoIsInCurrentSelectionUseCase()
            )
         } else {
            MediaMultiSelectionDisplay<Photo> (
                getInitialSelection = getInitialSelection,
                coroutineScope = viewModelScope,
                checkItemIsInToAddList = CheckPhotoIsInCurrentSelectionUseCase(),
                checkItemIsInToRemoveList = CheckPhotoIsInSelectionToRemoveUseCase(),
                checkItemIsInToKeepList = CheckPhotoIsInCurrentSelectionUseCase()
            )
        }
    val currentDisplayedPhotoSelection: StateFlow<List<CheckableItem<Photo>>> get() = mediaSelectionDisplay.currentDisplayedItems
    val selectionSize: Int get() = mediaSelectionDisplay.currentSelectionCount
    private val _toKeepButtonChecked = MutableLiveData(true)
    val toKeepButtonChecked: LiveData<Boolean> get() = _toKeepButtonChecked
    private val _toRemoveButtonChecked = MutableLiveData(true)
    val toRemoveButtonChecked: LiveData<Boolean> get() = _toRemoveButtonChecked
    private val _toAddButtonChecked = MutableLiveData(true)
    val toAddButtonChecked: LiveData<Boolean> get() = _toAddButtonChecked

    fun resetSelection() {
        mediaSelectionDisplay.resetSelection()
    }

    fun retrievePhotoSelectionFromPickerResults(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            mediaSelectionDisplay.addItems(mediaRepository.retrievePhotos(context, uris))
        }
    }

    fun toggle(photo: CheckableItem<Photo>) {
        mediaSelectionDisplay.toggleItem(photo)
    }

    fun toggleToKeepButton() {
        _toKeepButtonChecked.value?.let {
            _toKeepButtonChecked.value = !it
        }
    }

    fun toggleToRemoveButton() {
        _toRemoveButtonChecked.value?.let {
            _toRemoveButtonChecked.value = !it
        }
    }

    fun toggleToAddButton() {
        _toAddButtonChecked.value?.let {
            _toAddButtonChecked.value = !it
        }
    }

    fun setDisplayMode(displayMode: MediaMultiSelectionTracker.Display) {
        mediaSelectionDisplay.setDisplay(displayMode)
    }

    fun getPhotosToAdd(): List<Photo> {
        return mediaSelectionDisplay.getSelectionToAdd()
    }

    fun getPhotosToRemove(): List<Photo> {
        return mediaSelectionDisplay.getSelectionToRemove()
    }

    fun onSelectionOptionsQueryTextSubmit(query: String?) {
        query?.let {
            mediaSelectionDisplay.newQuery(query)
        }
    }

    fun onSelectionOptionsQueryTextChange(newText: String?) {
        newText?.let {
            mediaSelectionDisplay.newQuery(newText)
        }
    }

    fun isLoading(): Boolean {
        return mediaSelectionDisplay.isLoading()
    }

    fun loadMore() {
        return mediaSelectionDisplay.loadMoreItems()
    }

    fun onLastPage(): Boolean {
        return mediaSelectionDisplay.isLastPage()
    }
    companion object {
        fun provideFactory(
            mediaRepository: MediaRepository,
            maxItems: Int = Int.MAX_VALUE,
            getInitialSelection: GetMultipleInitialItemsUseCase<Photo>? = null,
            getAssociatedItems: GetItemsUseCase<Photo>? = null,
            getSearchedPhotos: GetSearchedItemsUseCase<Photo>? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MediaSelectionViewModel::class.java)) {
                    return MediaSelectionViewModel(
                        mediaRepository,
                        maxItems,
                        getInitialSelection,
                        getAssociatedItems,
                        getSearchedPhotos
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}