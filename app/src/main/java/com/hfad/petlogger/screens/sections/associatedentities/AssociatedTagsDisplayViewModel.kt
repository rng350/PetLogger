package com.hfad.petlogger.screens.sections.associatedentities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.util.Navigator
import com.hfad.petlogger.tags.data.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssociatedTagsDisplayViewModel(getAssociatedTagsUseCase: GetItemsUseCase<Tag>): ViewModel() {
    private val _tags: MutableStateFlow<List<Tag>> = MutableStateFlow<List<Tag>>(listOf<Tag>())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()
    val navigator = Navigator()

    init {
        viewModelScope.launch {
            val associatedTags = getAssociatedTagsUseCase()
            _tags.update { associatedTags }
        }
    }

    companion object {
        fun provideFactory(getAssociatedTags: GetItemsUseCase<Tag>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AssociatedTagsDisplayViewModel::class.java)) {
                    return AssociatedTagsDisplayViewModel(getAssociatedTags) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}