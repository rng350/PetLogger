package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.copyOf
import com.hfad.petlogger.common.usecases.factories.GetAllCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.factories.GetSearchedCurrentSelectionUseCaseFactory
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
    INITIAL SELECTION
    Pre-existing selection, prior to creation of display & dialog fragments.

    INITIAL NEW SELECTION
    Pre-existing selection not already associated with entity.
    Mostly for creating a new entity associated with another one
    For example, if you wanted to create a new Weight for a Pet, the Pet's ID will be associated with the Weight

    CURRENT SELECTION
    Items marked for becoming the new selection. Already confirmed from dialog selection. Changes not yet submitted to database.

    PROSPECTIVE SELECTION
    Items marked for selection in dialog, not yet confirmed

    VISIBLE OPTIONS
    The checkable options shown in the dialog. Can vary based depending on whatever query may be in, say, a search box.

    VISIBLE CURRENT SELECTION
    The current selection being displayed. May be a limited subset of "CURRENT SELECTION" depending on pagination and search.

    A note on fetchers:
    In Initialization:
    - getInitialSelection (gotta retrieve everything in one shot, no pagination)
    In Selection Display: Not paginated
    - getAllCurrentSelectionDisplay
    - getSearchedCurrentSelectionDisplay
    In Selection Dialog: Paginated
    - getAllSelectionOptions
    - getSearchedSelectionOptions
**/
class MultiSelectionTracker<T>(
    getInitialSelection: GetMultipleInitialItemsUseCase<T>? = null,
    private val getAllSelectionOptions: GetItemsUseCase<T>,
    private val getSearchedSelectionOptions: GetSearchedItemsUseCase<T>,
    getAllCurrentSelectionDisplayFactory: GetAllCurrentSelectionUseCaseFactory<T>,
    getSearchedCurrentSelectionDisplayFactory: GetSearchedCurrentSelectionUseCaseFactory<T>,
    private val coroutineScope: CoroutineScope,
    private val choiceLimit: Int = Int.MAX_VALUE
) {
    private val initialSelection = HashSet<T>()
    private val initialNewSelection = HashSet<T>()
    private val getSearchedCurrentSelectionDisplay: GetSearchedItemsUseCase<T>
    private val getAllCurrentSelectionDisplay: GetItemsUseCase<T>
    // for dialog
    private val _visibleOptions = MutableLiveData<List<CheckableItem<T>>>()
    val visibleOptions: LiveData<List<CheckableItem<T>>> get() = _visibleOptions
    private lateinit var visibleOptionsMap: Map<T, CheckableItem<T>>

    private val _currentSelection = MutableLiveData<List<T>>()
    val currentSelection: LiveData<List<T>> get() = _currentSelection
    // in-between
    private val _prospectiveSelection = MutableLiveData<List<T>>()
    val prospectiveSelection: LiveData<List<T>> get() = _prospectiveSelection
    private val _visibleCurrentSelection = MutableLiveData<List<T>>()
    val visibleCurrentSelection: LiveData<List<T>> get() = _visibleCurrentSelection

    private var _currentVisibleSelectionOptionsGetter: GetItemsUseCase<T> = getAllSelectionOptions
    private var _currentVisibleCurrentSelectionDisplayGetter: GetItemsUseCase<T>

    init {
        getAllCurrentSelectionDisplay = getAllCurrentSelectionDisplayFactory.createGetAllCurrentSelectionUseCase(currentSelection)
        getSearchedCurrentSelectionDisplay = getSearchedCurrentSelectionDisplayFactory.createGetSearchedCurrentSelectionUseCase(currentSelection)
        _currentVisibleCurrentSelectionDisplayGetter = getAllCurrentSelectionDisplay

        coroutineScope.launch {
            val allOptionsDeferred = async {
                getAllSelectionOptions()
            }
            when (getInitialSelection) {
                is GetMultipleInitialItemsUseCase.New -> {
                    val initialNewPicksDeferred = async {
                        getInitialSelection.useCase()
                    }
                    val initialNewPicks = initialNewPicksDeferred.await()
                    initialNewPicks?.let { newPick ->
                        initialNewSelection.add(newPick)
                    }
                }
                is GetMultipleInitialItemsUseCase.PreExisting -> {
                    val initialPicksDeferred = async {
                        getInitialSelection.useCase()
                    }
                    val initialPicks = initialPicksDeferred.await()
                    initialSelection.addAll(initialPicks)
                }
                else -> {}
            }
            val currentSelectionTemp = mutableListOf<T>()
            val visibleOptionsFetched = allOptionsDeferred.await().map {
                if (initialSelection.contains(it) || initialNewSelection.contains(it)) {
                    val checkableItem = CheckableItem(it, MutableLiveData(true))
                    currentSelectionTemp.add(checkableItem.item)
                    checkableItem
                } else {
                    CheckableItem(it, MutableLiveData(false))
                }
            }
            _visibleOptions.value = visibleOptionsFetched
            visibleOptionsMap = visibleOptionsFetched.associateBy { it.item }

            _currentSelection.value = currentSelectionTemp
            _visibleCurrentSelection.value = currentSelectionTemp
            _prospectiveSelection.value = currentSelectionTemp
        }
    }

    // call whenever search box is interacted with
    private fun setVisibleSelectionOptions(visibleOptionsFetcher: GetItemsUseCase<T>) {
        _currentVisibleSelectionOptionsGetter = visibleOptionsFetcher
        reloadVisibleSelectionOptions()
    }

    private fun reloadVisibleSelectionOptions() {
        coroutineScope.launch {
            _currentVisibleSelectionOptionsGetter.resetCurrentPoint()
            val visibleOptionsFetched = async {
                _currentVisibleSelectionOptionsGetter()
            }.await().map {
                CheckableItem(it, MutableLiveData(prospectiveSelection.value?.contains(it) ?: false))
            }
            _visibleOptions.value = visibleOptionsFetched
            visibleOptionsMap = visibleOptionsFetched.associateBy { it.item }
        }
    }

    // meant to be used with pagination
    fun loadVisibleSelectionOptions() {
        coroutineScope.launch {
            val visibleOptionsFetched = async {
                _currentVisibleSelectionOptionsGetter()
            }.await().map {
                CheckableItem(it, MutableLiveData(prospectiveSelection.value?.contains(it) ?: false))
            }
            _visibleOptions.value = (_visibleOptions.value ?: listOf()) + visibleOptionsFetched
            visibleOptionsMap = _visibleOptions.value?.associateBy { it.item } ?: mapOf()
        }
    }

    private fun reloadVisibleCurrentSelectionDisplay() {
        coroutineScope.launch {
            val visibleCurrentSelectionFetched = _currentVisibleCurrentSelectionDisplayGetter()
            _visibleCurrentSelection.value = visibleCurrentSelectionFetched
        }
    }
    /*fun loadVisibleCurrentSelectionDisplay() {
        coroutineScope.launch {
            val visibleCurrentSelectionFetched = currentVisibleCurrentSelectionDisplayGetter()
            _visibleCurrentSelection.value = (_visibleCurrentSelection.value ?: listOf()) + visibleCurrentSelectionFetched
        }
    }*/

    // call when pressing "Cancel" in dialog
    fun cancelProspectiveSelection() {
        // set prospective to current
        _visibleOptions.value = _visibleOptions.value?.onEach { it.isChecked.value = currentSelection.value?.contains(it.item) ?: false } ?: listOf()
        _prospectiveSelection.value = currentSelection.value
        // reset selection option view in dialog
        setVisibleSelectionOptions(getAllSelectionOptions)
    }

    // call when pressing "Ok" in dialog
    fun confirmProspectiveSelection() {
        // set current to prospective
        _currentSelection.value = prospectiveSelection.value
        setVisibleSelectionOptions(getAllSelectionOptions)
        reloadVisibleCurrentSelectionDisplay()
    }

    // call when pressing on any item in dialog
    fun toggle(checkableItem: CheckableItem<T>) {
        _prospectiveSelection.value?.let {
            // if in prospective, remove from it
            if (it.contains(checkableItem.item)) {
                checkableItem.isChecked.value = false
                val listCopy = it.toMutableList()
                listCopy.remove(checkableItem.item)
                _prospectiveSelection.value = listCopy
            } else {
                // if not in prospective, add to it
                if (prospectiveSelection.value!=null && prospectiveSelection.value!!.size<choiceLimit) {
                    checkableItem.isChecked.value = true
                    val listCopy = it.toMutableList()
                    listCopy.add(checkableItem.item)
                    _prospectiveSelection.value = listCopy
                }
            }
        }
    }

    fun searchFromSelectionOptions(query: String) {
        _currentVisibleSelectionOptionsGetter = if (query.isNotEmpty()) {
            getSearchedSelectionOptions.changeSearchQueryAndResetCurrentPoint(query)
            getSearchedSelectionOptions
        } else {
            getAllSelectionOptions.resetCurrentPoint()
            getAllSelectionOptions
        }
        reloadVisibleSelectionOptions()
    }

    fun searchFromCurrentSelectionDisplay(query: String) {
        _currentVisibleCurrentSelectionDisplayGetter = if (query.isNotEmpty()) {
            getSearchedCurrentSelectionDisplay.changeSearchQueryAndResetCurrentPoint(query)
            getSearchedCurrentSelectionDisplay
        } else {
            getAllCurrentSelectionDisplay.resetCurrentPoint()
            getAllCurrentSelectionDisplay
        }
        reloadVisibleCurrentSelectionDisplay()
    }

    fun visibleSelectionOptionsOnLastPage(): Boolean {
        return _currentVisibleSelectionOptionsGetter.onLastPage
    }

    fun visibleCurrentSelectionDisplayOnLastPage(): Boolean {
        return _currentVisibleCurrentSelectionDisplayGetter.onLastPage
    }

    // call when submitting in display fragment
    fun getSelectionToAdd(): List<T> {
        // anything in current that's not in initial selection
        val selectionTemp = mutableListOf<T>()
        currentSelection.value?.let { currentSelectionList ->
            selectionTemp.addAll(currentSelectionList.map{it}.filterNot{initialSelection.contains(it)})
        }
        return selectionTemp.toList()
    }

    // call when submitting in display fragment
    fun getSelectionToRemove(): List<T> {
        // anything in initial that's not in current
        val selectionTemp = mutableListOf<T>()
        val initialSelectionAsList = initialSelection.toList()
        currentSelection.value?.let { currentSelectionList ->
            val currentSelectionHash = currentSelectionList.map{it}.toHashSet()
            selectionTemp.addAll(initialSelectionAsList.filterNot { currentSelectionHash.contains(it) })
        }
        return selectionTemp
    }

    // call when clicking on item from display fragment
    fun remove(item: T) {
        // remove from both current & prospective selection lists
        val checkedItem = visibleOptionsMap.get(item)
        checkedItem?.isChecked?.value = false
        _prospectiveSelection.value?.let {
            val listCopy = it.toMutableList()
            listCopy.remove(item)
            _prospectiveSelection.value = listCopy
        }
        _currentSelection.value?.let {
            val listCopy = it.toMutableList()
            listCopy.remove(item)
            _currentSelection.value = listCopy
        }
        _visibleCurrentSelection.value?.let {
            val listCopy = it.toMutableList()
            listCopy.remove(item)
            _visibleCurrentSelection.value = listCopy
        }
    }

    // call when resetting in display fragment
    fun resetSelection() {
        val currentSelectionTemp = mutableListOf<T>()

        _visibleOptions.value = visibleOptions.value?.onEach {
            if (initialSelection.contains(it.item) || initialNewSelection.contains(it.item)) {
                it.isChecked.value = true
                currentSelectionTemp.add(it.item)
            } else {
                it.isChecked.value = false
            }
        }?.copyOf() ?: listOf()

        _currentSelection.value = currentSelectionTemp
        _prospectiveSelection.value = currentSelectionTemp
    }
}