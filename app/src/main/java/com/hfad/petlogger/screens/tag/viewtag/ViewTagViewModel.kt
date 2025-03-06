package com.hfad.petlogger.screens.tag.viewtag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.domain.TagRepository
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