package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.photodisplay.stateful.GetAllPhotosForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FullGalleryViewModel(getAllPhotos: GetAllPhotosForDisplayUseCase) : ViewModel() {
    val photos = getAllPhotos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf()
    )
    val photoNavigator = Navigator()
    companion object {
        fun provideFactory(getAllPhotos: GetAllPhotosForDisplayUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FullGalleryViewModel::class.java)) {
                    return FullGalleryViewModel(getAllPhotos) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }

}