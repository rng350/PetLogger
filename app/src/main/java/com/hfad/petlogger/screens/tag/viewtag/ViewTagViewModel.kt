package com.hfad.petlogger.screens.tag.viewtag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.events.EventForList
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.pets.PetWithProfilePic
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.WeightForList
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.common.util.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewTagViewModel(tagRepository: TagRepository, tagId: Long) : ViewModel() {
    private val _tag: MutableLiveData<Tag> = MutableLiveData()
    val tag: LiveData<Tag> get() = _tag

    init {
        viewModelScope.launch {
            _tag.value = tagRepository.getTag(tagId)
        }
    }
    companion object {
        fun provideFactory(tagRepository: TagRepository, tagId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ViewTagViewModel::class.java)) {
                    return ViewTagViewModel(tagRepository, tagId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}