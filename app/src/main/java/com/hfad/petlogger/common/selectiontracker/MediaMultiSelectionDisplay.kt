package com.hfad.petlogger.common.selectiontracker

import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetMultipleInitialItemsUseCase
import com.hfad.petlogger.common.usecases.GetSearchedItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
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
    private val getSearchedItems: GetSearchedItemsUseCase<T> =
        object : GetSearchedItemsUseCase<T> {
            override var currentQuery: String = ""
            override val onLastPage: Boolean = true
            override suspend fun invoke(): List<T> {
                return listOf()
            }
            override fun resetCurrentPoint() {}
        },
    private val getAssociatedItemsPaginated: GetItemsUseCase<T> =
        object: GetItemsUseCase<T> {
            override val onLastPage: Boolean = true
            override suspend fun invoke(): List<T> {
                return listOf()
            }
            override fun resetCurrentPoint() {}
        },
    private val coroutineScope: CoroutineScope,
    checkItemIsInToAddList: CheckItemIsInSelectionUseCase<T>,
    checkItemIsInToRemoveList: CheckItemIsInSelectionUseCase<T>,
    checkItemIsInToKeepList: CheckItemIsInSelectionUseCase<T>
) {
    private val selectionTracker = MediaMultiSelectionTracker(
        checkItemIsInToAddList,
        checkItemIsInToRemoveList,
        checkItemIsInToKeepList
    )
    private val _currentDisplayedItems: MutableStateFlow<List<CheckableItem<T>>> = MutableStateFlow(listOf())
    val currentDisplayedItems: StateFlow<List<CheckableItem<T>>> = _currentDisplayedItems
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )
    private var currentDisplayMode: MediaMultiSelectionTracker.Display = MediaMultiSelectionTracker.Display.All
    private var currentItemFetcher: GetItemsUseCase<T> = getAssociatedItemsPaginated
    private var allQueriedItems: List<T> = listOf()
    private var _isLoading: AtomicBoolean = AtomicBoolean(false)
    val currentSelectionCount: Int get() = selectionTracker.currentSelectionCount

    init {
        coroutineScope.launch {
            _isLoading.set(true)
            val initialSelectionFetched = async {
                when (getInitialSelection) {
                    is GetMultipleInitialItemsUseCase.New -> {
                        val initialNewItem = getInitialSelection.useCase()
                        initialNewItem?.let {
                            selectionTracker.initializeSelectionToAdd(it)
                        }
                    }
                    is GetMultipleInitialItemsUseCase.PreExisting -> {
                        val initialNewItems = getInitialSelection.useCase()
                        selectionTracker.initializeSelectionToKeep(initialNewItems)
                    }
                    null -> {}
                }
            }
            val initialDisplaySelectionFetched = async {
                allQueriedItems = getAssociatedItemsPaginated()
            }
            initialDisplaySelectionFetched.await()
            initialSelectionFetched.await()
            val filteredSelection = selectionTracker.filterQueriedItemsForDisplay(
                allQueriedItems,
                currentDisplayMode
            )
            _currentDisplayedItems.update { filteredSelection }
            _isLoading.set(false)
        }
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
            getAssociatedItemsPaginated.resetCurrentPoint()
            getAssociatedItemsPaginated
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
            allQueriedItems,
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