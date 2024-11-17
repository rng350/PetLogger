package com.hfad.petlogger.screens.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.common.util.NewEntityNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullGalleryViewModel(private val getAllPhotos: GetItemsUseCase<Photo>) : ViewModel() {
    private val _photos: MutableStateFlow<List<Photo>> = MutableStateFlow(listOf())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    val photoNavigator = Navigator()
    val newPhotoNavigator = NewEntityNavigator()
    private var isLoading: Boolean = false
    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedPhotos = getAllPhotos()
            _photos.update { it + loadedPhotos }
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAllPhotos.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }
    companion object {
        fun provideFactory(getAllPhotos: GetItemsUseCase<Photo>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FullGalleryViewModel::class.java)) {
                    return FullGalleryViewModel(getAllPhotos) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }

}