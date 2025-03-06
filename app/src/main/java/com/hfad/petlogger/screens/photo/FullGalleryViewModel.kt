package com.hfad.petlogger.screens.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.common.util.NewEntityNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullGalleryViewModel(
    private val getInitialPhotos: GetItemsUseCase<Photo>,
    private val getSearchedPhotos: GetSearchedItemsUseCase<Photo>
) : ViewModel() {
    private var currentPhotoGetter: GetItemsUseCase<Photo> = getInitialPhotos
    private val _photos: MutableStateFlow<List<Photo>> = MutableStateFlow(listOf())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    val photoNavigator = Navigator()
    val newPhotoNavigator = NewEntityNavigator()
    private var isLoading: Boolean = false
    init {
        reload()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedPhotos = currentPhotoGetter()
            _photos.update { it + loadedPhotos }
            isLoading = false
        }
    }
    private fun reload() {
        viewModelScope.launch {
            isLoading = true
            val loadedPhotos = currentPhotoGetter()
            _photos.update { loadedPhotos }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getInitialPhotos.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }

    fun onQueryTextSubmit(query: String?) {
        if (query != null) {
            reinitializeGetterType(query)
        }
    }

    fun onQueryTextChanged(newText: String?) {
        if (newText != null) {
            reinitializeGetterType(newText)
        }
    }

    private fun reinitializeGetterType(query: String) {
        if (query.isNotEmpty()) {
            getSearchedPhotos.changeSearchQueryAndResetCurrentPoint(query)
            currentPhotoGetter = getSearchedPhotos
        } else {
            currentPhotoGetter = getInitialPhotos
            currentPhotoGetter.resetCurrentPoint()
        }
        reload()
    }
    companion object {
        fun provideFactory(getAllPhotos: GetItemsUseCase<Photo>, getSearchedPhotos: GetSearchedItemsUseCase<Photo>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FullGalleryViewModel::class.java)) {
                    return FullGalleryViewModel(getAllPhotos, getSearchedPhotos) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }

}