package com.hfad.petlogger.common.selectiontracker

import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// put in ViewModel
// allQueriedItems = All items queried, regardless of whether or not they're present in the current (de)selection
class MultiDeselectionDisplay<T>(
    private val getSearchedItems: GetSearchedItemsUseCase<T>,
    private val getAllAssociatedItems: GetItemsUseCase<T>,
    private val coroutineScope: CoroutineScope
) {
    private val multiDeselectionTracker = MultiDeselectionTracker<T>()
    private val _currentDisplayedItems: MutableStateFlow<List<CheckableItem<T>>> = MutableStateFlow(listOf())
    val currentDisplayedItems: StateFlow<List<CheckableItem<T>>> = _currentDisplayedItems
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )
    private val _currentDisplayMode: MutableStateFlow<MultiDeselectionTracker.Display> = MutableStateFlow(MultiDeselectionTracker.Display.All)
    val currentDisplayMode: StateFlow<MultiDeselectionTracker.Display> = _currentDisplayMode
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MultiDeselectionTracker.Display.All
        )
    private var currentItemFetcher: GetItemsUseCase<T> = getAllAssociatedItems
    private var allQueriedItems: List<T> = listOf()
    private var _isLoading: AtomicBoolean = AtomicBoolean(false)

    init {
        reloadItems()
    }

    fun toggleItem(checkableItem: CheckableItem<T>) {
        multiDeselectionTracker.toggle(checkableItem)
    }

    fun setDisplay(display: MultiDeselectionTracker.Display) {
        if (_currentDisplayMode.value != display) {
            _currentDisplayMode.update { display }
            _currentDisplayedItems.update {
                multiDeselectionTracker.filterQueriedItemsForDisplay(
                    allQueriedItems,
                    _currentDisplayMode.value
                )
            }
        }
    }

    fun newQuery(query: String) {
        currentItemFetcher = if (query.isNotEmpty()) {
            getSearchedItems.changeSearchQueryAndResetCurrentPoint(query)
            getSearchedItems
        } else {
            getAllAssociatedItems.resetCurrentPoint()
            getAllAssociatedItems
        }
        reloadItems()
    }

    private fun reloadItems() {
        coroutineScope.launch {
            _isLoading.set(true)
            allQueriedItems = currentItemFetcher()
            val filteredSelection = multiDeselectionTracker.filterQueriedItemsForDisplay(
                allQueriedItems,
                _currentDisplayMode.value
            )
            _currentDisplayedItems.update { filteredSelection }
            _isLoading.set(false)
        }
    }

    fun loadMoreItems() {
        coroutineScope.launch {
            _isLoading.set(true)
            val newQueriedItems = currentItemFetcher()
            allQueriedItems = allQueriedItems + newQueriedItems
            val filteredSelection = multiDeselectionTracker.filterQueriedItemsForDisplay(
                newQueriedItems,
                _currentDisplayMode.value
            )
            _currentDisplayedItems.update { it + filteredSelection }
            _isLoading.set(false)
        }
    }

    fun resetSelection() {
        multiDeselectionTracker.resetSelection()
        _currentDisplayedItems.update{it.onEach { item -> item.isChecked.value = false }}
    }

    fun isLastPage(): Boolean {
        return currentItemFetcher.onLastPage
    }

    fun isLoading(): Boolean {
        return _isLoading.get()
    }

    fun getSelectionToRemove() = multiDeselectionTracker.getSelectionToRemove()
}