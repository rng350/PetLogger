package com.hfad.petlogger.screens.tag.tagmultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.usecases.GetSearchedTagsUseCase
import com.hfad.petlogger.tags.TagRepository
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.usecases.GetAllTagsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.tags.usecases.GetAllTagsUseCase
import com.hfad.petlogger.tags.usecases.GetSearchedTagsFromCurrentSelectionUseCase
import com.hfad.petlogger.tags.usecases.GetSearchedTagsFromCurrentSelectionUseCaseFactory
import kotlinx.coroutines.launch

class TagMultiSelectionViewModel(
    private val tagRepository: TagRepository,
    private val getAllTags: GetAllTagsUseCase,
    getAllSearchedTagsUseCase: GetSearchedTagsUseCase,
    getAllCurrentSelectionFactory: GetAllTagsFromCurrentSelectionUseCaseFactory,
    getSearchedTagsFromCurrentSelectionFactory: GetSearchedTagsFromCurrentSelectionUseCaseFactory,
    getInitialSelection: GetMultipleInitialItemsUseCase<Tag>? = null
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<Tag>(
        getAllSelectionOptions = getAllTags,
        getInitialSelection = getInitialSelection,
        getSearchedSelectionOptions = getAllSearchedTagsUseCase,
        getAllCurrentSelectionDisplayFactory = getAllCurrentSelectionFactory,
        getSearchedCurrentSelectionDisplayFactory = getSearchedTagsFromCurrentSelectionFactory,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged
    private var visibleSelectionOptionsLoading: Boolean = false

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

    fun onCurrentSelectionDisplayQueryTextSubmit(query: String?) {
        query?.let {
            selectionTracker.searchFromCurrentSelectionDisplay(query)
        }
    }

    fun onCurrentSelectionDisplayQueryTextChange(newText: String?) {
        newText?.let {
            selectionTracker.searchFromCurrentSelectionDisplay(newText)
        }
    }

    fun onSelectionOptionsQueryTextSubmit(query: String?) {
        query?.let {
            selectionTracker.searchFromSelectionOptions(query)
        }
    }

    fun onSelectionOptionsQueryTextChange(newText: String?) {
        newText?.let {
            selectionTracker.searchFromSelectionOptions(newText)
        }
    }

    fun loadFromVisibleOptions() {
        viewModelScope.launch {
            visibleSelectionOptionsLoading = true
            selectionTracker.loadVisibleSelectionOptions()
            visibleSelectionOptionsLoading = false
        }
    }

    fun visibleOptionsAreLoading(): Boolean {
        return visibleSelectionOptionsLoading
    }

    fun visibleOptionsOnLastPage(): Boolean {
        return selectionTracker.visibleSelectionOptionsOnLastPage()
    }

    companion object {
        fun provideFactory(
            tagRepository: TagRepository,
            getAllTags: GetAllTagsUseCase,
            getAllSearchedTagsUseCase: GetSearchedTagsUseCase,
            getAllCurrentSelectionFactory: GetAllTagsFromCurrentSelectionUseCaseFactory,
            getSearchedTagsFromCurrentSelectionFactory: GetSearchedTagsFromCurrentSelectionUseCaseFactory,
            getInitialSelection: GetMultipleInitialItemsUseCase<Tag>? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TagMultiSelectionViewModel::class.java)) {
                    return TagMultiSelectionViewModel(
                        tagRepository,
                        getAllTags,
                        getAllSearchedTagsUseCase,
                        getAllCurrentSelectionFactory,
                        getSearchedTagsFromCurrentSelectionFactory,
                        getInitialSelection
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}