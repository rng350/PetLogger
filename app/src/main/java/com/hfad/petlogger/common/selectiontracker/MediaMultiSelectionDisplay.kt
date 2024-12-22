package com.hfad.petlogger.common.selectiontracker

import android.util.Log
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// getAllAssociatedItems = The union of selection to keep & selection to remove at all times
class MediaMultiSelectionDisplay<T>(
    getInitialSelection: GetMultipleInitialItemsUseCase<T>? = null,
    getSearchedItemsArg: GetSearchedItemsUseCase<T>? = null,
    private val coroutineScope: CoroutineScope,
    checkItemIsInToAddList: CheckItemIsInSelectionUseCase<T>,
    checkItemIsInToRemoveList: CheckItemIsInSelectionUseCase<T>,
    checkItemIsInToKeepList: CheckItemIsInSelectionUseCase<T>
) {
    private val selectionTracker = MediaMultiSelectionTracker(
        getInitialSelection,
        checkItemIsInToAddList,
        checkItemIsInToRemoveList,
        checkItemIsInToKeepList,
        coroutineScope
    )
    private val getSearchedItems: GetSearchedItemsUseCase<T> =
        getSearchedItemsArg
            ?: object : GetSearchedItemsUseCase<T> {
                override var currentQuery: String = ""
                override val onLastPage: Boolean = true
                override suspend fun invoke(): List<T> {
                    return listOf()
                }
                override fun resetCurrentPoint() {}
            }
    private val _currentDisplayedItems: MutableStateFlow<List<CheckableItem<T>>> = MutableStateFlow(listOf())
    val currentDisplayedItems: StateFlow<List<CheckableItem<T>>> = _currentDisplayedItems
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )
    private var currentDisplayMode: MediaMultiSelectionTracker.Display = MediaMultiSelectionTracker.Display.All
    private val getAllAssociatedItems: GetItemsUseCase<T> =
        if (getInitialSelection is GetMultipleInitialItemsUseCase.PreExisting)
            getInitialSelection.useCase
        // get nothing if there's no initial selection to draw from
        else object: GetItemsUseCase<T> {
            override val onLastPage: Boolean = true
            override suspend fun invoke(): List<T> {
                return listOf()
            }
            override fun resetCurrentPoint() {}
        }
    private var currentItemFetcher: GetItemsUseCase<T> = getAllAssociatedItems
    private var allQueriedItems: List<T> = listOf()
    private var _isLoading: AtomicBoolean = AtomicBoolean(false)
    val currentSelectionCount: Int get() = selectionTracker.currentSelectionCount

    init {
        reloadItems()
    }

    fun toggleItem(checkableItem: CheckableItem<T>) {
        selectionTracker.toggle(checkableItem)
        _currentDisplayedItems.update { selectionTracker.filterQueriedItemsForDisplay(allQueriedItems, currentDisplayMode) }
    }

    fun setDisplay(display: MediaMultiSelectionTracker.Display) {
        currentDisplayMode = display
        _currentDisplayedItems.update {
            selectionTracker.filterQueriedItemsForDisplay(
                allQueriedItems,
                currentDisplayMode
            )
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

    fun addItems(items: List<T>) {
        val selectionChanged = selectionTracker.addNewItems(items)
        if (selectionChanged) {
            val filteredSelection = selectionTracker.filterQueriedItemsForDisplay(
                allQueriedItems,
                currentDisplayMode
            )
            _currentDisplayedItems.update { filteredSelection }
        }
    }

    private fun reloadItems() {
        coroutineScope.launch {
            _isLoading.set(true)
            allQueriedItems = currentItemFetcher()
            val filteredSelection = selectionTracker.filterQueriedItemsForDisplay(
                allQueriedItems,
                currentDisplayMode
            )
            _currentDisplayedItems.update { filteredSelection }
            _isLoading.set(false)
        }
    }

    fun loadMoreItems() {
        coroutineScope.launch {
            Log.d("MediaMultiSelDisplay", "LoadMore called...")
            _isLoading.set(true)
            val newQueriedItems = currentItemFetcher()
            val filteredSelection = selectionTracker.filterQueriedItemsForDisplay(
                newQueriedItems,
                currentDisplayMode
            )
            allQueriedItems = allQueriedItems + newQueriedItems
            _currentDisplayedItems.update { it + filteredSelection }
            _isLoading.set(false)
        }
    }

    fun resetSelection() {
        selectionTracker.resetSelection()
        val filteredSelection = selectionTracker.filterQueriedItemsForDisplay(
            currentDisplayedItems.value.map{it.item},
            currentDisplayMode
        )
        _currentDisplayedItems.update{ filteredSelection }
    }

    fun isLastPage(): Boolean {
        return currentItemFetcher.onLastPage
    }

    fun isLoading(): Boolean {
        return _isLoading.get()
    }

    fun getSelectionToAdd() = selectionTracker.getSelectionToAdd()
    fun getSelectionToRemove() = selectionTracker.getSelectionToRemove()
}