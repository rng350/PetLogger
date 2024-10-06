package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Photo
import com.hfad.petlogger.photodisplay.stateful.GetItemsForDisplayUseCase
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.util.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedPhotosDisplayViewModel(private val getAssociatedPhotos: GetItemsUseCase<Photo>) : ViewModel() {
    private val _photos: MutableStateFlow<List<Photo>> = MutableStateFlow<List<Photo>>(listOf<Photo>())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    val navigator = Navigator()
    private var isLoading: Boolean = false

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val loadedPhotos = getAssociatedPhotos()
            Log.d("AssocWeightsVM", "Loaded Events Size: ${loadedPhotos.size}")
            Log.d("AssocWeightsVM", "List Size Before: ${photos.value.size}")
            _photos.update { it + loadedPhotos }
            Log.d("AssocWeightsVM", "List Size After: ${photos.value.size}")
            isLoading = false
        }
    }

    fun onLastPage(): Boolean {
        return getAssociatedPhotos.onLastPage
    }

    fun isLoading(): Boolean {
        return isLoading
    }
    companion object {
        fun provideFactory(getAssociatedPhotos: GetItemsUseCase<Photo>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedPhotosDisplayViewModel::class.java)) {
                    return AssociatedPhotosDisplayViewModel(getAssociatedPhotos) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}