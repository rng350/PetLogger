package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.photodisplay.GetAssociatedPhotosUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AssociatedPhotosDisplayViewModel(getAssociatedPhotos: GetAssociatedPhotosUseCase) : ViewModel() {
    private val _photos = MutableStateFlow(listOf<Photo>())
    val photos: StateFlow<List<Photo>> get() = _photos
    val navigator = Navigator()

    init {
        viewModelScope.launch {
            getAssociatedPhotos()
                .collect { collectedPhotos ->
                Log.d("FlowCollect", "collecting: ${collectedPhotos}")
                _photos.value = collectedPhotos
            }
        }
    }

    companion object {
        fun provideFactory(getAssociatedPhotos: GetAssociatedPhotosUseCase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPhotosDisplayViewModel::class.java)) {
                    return AssociatedPhotosDisplayViewModel(getAssociatedPhotos) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}