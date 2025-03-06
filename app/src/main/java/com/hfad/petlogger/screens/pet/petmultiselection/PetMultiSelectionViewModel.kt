package com.hfad.petlogger.screens.pet.petmultiselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.petlogger.common.selectiontracker.MultiSelectionTracker
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.pets.data.PetWithProfilePic
import com.hfad.petlogger.pets.domain.usecases.GetAllPetsFromCurrentSelectionUseCaseFactory
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfAllPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetMoreOfSearchedPetsUseCase
import com.hfad.petlogger.pets.domain.usecases.GetSearchedPetsFromCurrentSelectionUseCaseFactory
import kotlinx.coroutines.launch

// should be included in main fragment too
class PetMultiSelectionViewModel(
    getAllPets: GetMoreOfAllPetsUseCase,
    getInitialSelection: GetMultipleInitialItemsUseCase<PetWithProfilePic>? = null,
    getSearchedSelectionOptions: GetMoreOfSearchedPetsUseCase,
    getAllCurrentSelectionDisplayFactory: GetAllPetsFromCurrentSelectionUseCaseFactory,
    getSearchedCurrentSelectionDisplayFactory: GetSearchedPetsFromCurrentSelectionUseCaseFactory
) : ViewModel() {
    val selectionTracker = MultiSelectionTracker<PetWithProfilePic>(
        getAllSelectionOptions = getAllPets,
        getSearchedSelectionOptions = getSearchedSelectionOptions,
        getInitialSelection = getInitialSelection,
        getAllCurrentSelectionDisplayFactory = getAllCurrentSelectionDisplayFactory,
        getSearchedCurrentSelectionDisplayFactory = getSearchedCurrentSelectionDisplayFactory,
        coroutineScope = viewModelScope
    )
    private var _currentSelectionChanged = false
    val currentSelectionChanged get() = _currentSelectionChanged
    private var visibleSelectionOptionsLoading: Boolean = false

    fun getPetsToAdd(): List<Long> {
        return selectionTracker.getSelectionToAdd().map{it.petId}
    }

    fun getPetsToRemove(): List<Long> {
        return selectionTracker.getSelectionToRemove().map{it.petId}
    }

    fun confirmSelection() {
        selectionTracker.confirmProspectiveSelection()
        _currentSelectionChanged = true
    }

    fun onCurrentSelectionChanged() {
        _currentSelectionChanged = false
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

    fun reset() {
        selectionTracker.resetSelection()
    }

    fun cancel() {
        selectionTracker.cancelProspectiveSelection()
    }

    companion object {
        fun provideFactory(
            getAllPets: GetMoreOfAllPetsUseCase,
            getInitialSelection: GetMultipleInitialItemsUseCase<PetWithProfilePic>? = null,
            getSearchedSelectionOptions: GetMoreOfSearchedPetsUseCase,
            getAllCurrentSelectionDisplayFactory: GetAllPetsFromCurrentSelectionUseCaseFactory,
            getSearchedCurrentSelectionDisplayFactory: GetSearchedPetsFromCurrentSelectionUseCaseFactory
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PetMultiSelectionViewModel::class.java)) {
                    return PetMultiSelectionViewModel(
                        getAllPets,
                        getInitialSelection,
                        getSearchedSelectionOptions,
                        getAllCurrentSelectionDisplayFactory,
                        getSearchedCurrentSelectionDisplayFactory
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}