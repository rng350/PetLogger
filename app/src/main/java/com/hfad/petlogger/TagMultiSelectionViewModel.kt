package com.hfad.petlogger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.photodisplay.stateless.GetItemsUseCase
import com.hfad.petlogger.photodisplay.stateless.GetMoreOfSearchedNotesFromAllUseCase
import com.hfad.petlogger.photodisplay.stateless.GetSearchedTagsUseCase
import com.hfad.petlogger.repositories.TagRepository
import com.hfad.petlogger.selectiontracker.MultiSelectionTracker
import kotlinx.coroutines.flow.update

class TagMultiSelectionViewModel(
    private val tagRepository: TagRepository,
    private val getAllTags: GetItemsUseCase<Tag>,
    getInitialSelection: GetItemsUseCase<Tag>? = null
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<Tag>(
        allOptionsFetcher = getAllTags,
        initialSelectionFetcher = getInitialSelection,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    private var currentFetcherGetsAllTags = true
    val currentSelectionChanged get() = _currentSelectionChanged

    fun getTagsToAdd(): List<Tag> {
        return selectionTracker.getSelectionToAdd()
    }

    fun getTagsToRemove(): List<Tag> {
        return selectionTracker.getSelectionToRemove()
    }

    fun confirmSelection() {
        selectionTracker.confirmProspectiveSelection()
        _currentSelectionChanged = true
    }

    fun onCurrentSelectionChanged() {
        _currentSelectionChanged = false
    }

    fun reset() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
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
            selectionTracker.setVisibleSelectionOptions(GetSearchedTagsUseCase(tagRepository, query))
            currentFetcherGetsAllTags = false
        } else {
            if (!currentFetcherGetsAllTags) {
                selectionTracker.setVisibleSelectionOptions(getAllTags)
                currentFetcherGetsAllTags = true
            }
        }
    }

    companion object {
        fun provideFactory(tagRepository: TagRepository, getAllTags: GetItemsUseCase<Tag>, getInitialSelection: GetItemsUseCase<Tag>? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TagMultiSelectionViewModel::class.java)) {
                    return TagMultiSelectionViewModel(tagRepository, getAllTags, getInitialSelection) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}