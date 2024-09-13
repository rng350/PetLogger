package com.hfad.petlogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase

class TagMultiSelectionViewModel(
    getAllTags: GetItemsUseCase<Tag>,
    getInitialSelection: GetItemsUseCase<Tag>? = null
) : ViewModel() {
    companion object {
        fun provideFactory(getAllTags: GetItemsUseCase<Tag>, getInitialSelection: GetItemsUseCase<Tag>? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TagMultiSelectionViewModel::class.java)) {
                    return TagMultiSelectionViewModel(getAllTags, getInitialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}