package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AssociatedPhotosDisplayViewModel(getAssociatedPhotos: GetItemsForDisplayUseCase<Photo>) : ViewModel() {
    val photos: StateFlow<List<Photo>> = getAssociatedPhotos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf<Photo>()
        )
    val navigator = Navigator()
    companion object {
        fun provideFactory(getAssociatedPhotos: GetItemsForDisplayUseCase<Photo>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPhotosDisplayViewModel::class.java)) {
                    return AssociatedPhotosDisplayViewModel(getAssociatedPhotos) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}