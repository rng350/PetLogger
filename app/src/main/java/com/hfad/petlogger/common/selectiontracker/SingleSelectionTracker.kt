package com.hfad.petlogger.common.selectiontracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSingleInitialItemUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
INITIAL SELECTION
Pre-existing selection, prior to creation of display & dialog fragments.

CURRENT SELECTION
Items marked for becoming the new selection. Already confirmed from dialog selection. Changes not yet submitted to database.

PROSPECTIVE SELECTION
Items marked for selection in dialog, not yet confirmed
 **/
class SingleSelectionTracker<T>(
    private val allOptionsFetcher: GetItemsUseCase<T>,
    private val initialItemFetcher: GetSingleInitialItemUseCase<T>?,
    private val coroutineScope: CoroutineScope
) {
    private var initialSelection: T? = null
    private var initialNewSelection: T? = null
    // for dialog
    private val _visibleOptions = MutableLiveData<List<CheckableItem<T>>>(listOf())
    val visibleOptions: LiveData<List<CheckableItem<T>>> get() = _visibleOptions
    private lateinit var visibleOptionsMap: Map<T, CheckableItem<T>>
    // for displaying
    private val _currentSelection = MutableLiveData<T?>()
    val currentSelection: LiveData<T?> get() = _currentSelection
    // in-between
    private val _prospectiveSelection = MutableLiveData<T?>()
    val prospectiveSelection: LiveData<T?> get() = _prospectiveSelection
    init {
        coroutineScope.launch {
            val allOptionsDeferred = async {
                allOptionsFetcher()
            }
            when (initialItemFetcher) {
                is GetSingleInitialItemUseCase.New -> {
                    val initialNewPickDeferred = async {
                        initialItemFetcher.useCase()
                    }
                    val initialNewPick = initialNewPickDeferred.await()
                    initialNewSelection = initialNewPick
                }
                is GetSingleInitialItemUseCase.PreExisting -> {
                    val initialPickDeferred = async {
                        initialItemFetcher.useCase()
                    }
                    val initialPick = initialPickDeferred.await()
                    initialSelection = initialPick
                }
                else -> { }
            }
            var currentSelectionTemp: T? = null
            val visibleOptionsFetched = allOptionsDeferred.await().map {
                if (initialSelection?.equals(it)==true || initialNewSelection?.equals(it)==true) {
                    val checkableItem = CheckableItem(it, MutableLiveData(true))
                    currentSelectionTemp = checkableItem.item
                    checkableItem
                } else {
                    CheckableItem(it, MutableLiveData(false))
                }
            }
            _visibleOptions.value = visibleOptionsFetched
            visibleOptionsMap = visibleOptionsFetched.associateBy { it.item }

            currentSelectionTemp?.let {
                _currentSelection.value = it
                _prospectiveSelection.value = it
            } ?: run {
                _currentSelection.value = null
                _prospectiveSelection.value = null
            }
        }
    }

    // call when pressing "Cancel" in dialog
    fun cancelProspectiveSelection() {
        _visibleOptions.value = _visibleOptions.value?.onEach { it.isChecked.value = currentSelection.value?.equals(it.item) ?: false } ?: listOf()
        _prospectiveSelection.value = currentSelection.value
        setVisibleSelectionOptions(allOptionsFetcher)
    }

    // call when pressing "Ok" in dialog
    fun confirmProspectiveSelection() {
        _currentSelection.value = prospectiveSelection.value
        setVisibleSelectionOptions(allOptionsFetcher)
    }

    // call when pressing on any item in dialog
    fun toggle(checkableItem: CheckableItem<T>) {
        //uncheck current prospective selection
        _prospectiveSelection.value?.let { curProspectiveSelection ->
            if (curProspectiveSelection != checkableItem.item) {
                visibleOptionsMap[curProspectiveSelection]?.isChecked?.value = false
            }
        }
        //check new prospective selection
        checkableItem.item?.let {
            _prospectiveSelection.value = it
            checkableItem.isChecked.value = true
        }
    }

    // call when submitting in display fragment
    fun getCurrentSelection(): T? {
        return currentSelection.value
    }

    // call when resetting in display fragment
    fun resetSelection() {
        when (initialItemFetcher) {
            is GetSingleInitialItemUseCase.New -> {
                initialNewSelection?.let {
                    _currentSelection.value = it
                }
            }
            is GetSingleInitialItemUseCase.PreExisting -> {
                initialSelection?.let {
                    _currentSelection.value = it
                }
            }
            else -> { }
        }
        cancelProspectiveSelection()
    }

    // call whenever search box is interacted with
    fun setVisibleSelectionOptions(visibleOptionsFetcher: GetItemsUseCase<T>) {
        coroutineScope.launch {
            val visibleOptionsFetched = async {
                visibleOptionsFetcher()
            }.await().map {
                CheckableItem(it, MutableLiveData(prospectiveSelection.value?.equals(it) ?: false))
            }
            _visibleOptions.value = visibleOptionsFetched
            visibleOptionsMap = visibleOptionsFetched.associateBy { it.item }
        }
    }
}