package com.xenon.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.xenon.todolist.SharedPreferenceManager
import com.xenon.todolist.viewmodel.classes.Priority
import com.xenon.todolist.viewmodel.classes.TaskItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


enum class SortOption {
    FREE_SORTING,
    CREATION_DATE,
    DUE_DATE,
    COMPLETENESS,
    NAME,
    IMPORTANCE
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

enum class FilterState {
    INCLUDED,
    EXCLUDED
}

enum class FilterableAttribute {
    HAS_DESCRIPTION,
    IS_LOW_PRIORITY,
    IS_HIGH_PRIORITY,
    IS_HIGHEST_PRIORITY,
    HAS_DUE_DATE,
    HAS_DUE_TIME;

    fun toDisplayString(): String {
        return when (this) {
            HAS_DESCRIPTION -> "Has Description"
            IS_LOW_PRIORITY -> "Low Importance"
            IS_HIGH_PRIORITY -> "High Importance"
            IS_HIGHEST_PRIORITY -> "Highest Importance"
            HAS_DUE_DATE -> "Has Due Date"
            HAS_DUE_TIME -> "Has Due Time"
        }
    }
}


class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)
    private val _allTaskItems = mutableStateListOf<TaskItem>()
    private val _displayedTaskItems = mutableStateListOf<TaskItem>()
    val taskItems: List<TaskItem> get() = _displayedTaskItems
    private var currentTaskId = 1

    var currentSelectedListId: String? = DEFAULT_LIST_ID
        set(value) {
            if (field != value) {
                field = value
                applySortingAndFiltering()
            }
        }

    var currentSortOption: SortOption by mutableStateOf(SortOption.FREE_SORTING)
        private set
    var currentSortOrder: SortOrder by mutableStateOf(SortOrder.ASCENDING)
        private set

    var filterStates = mutableStateMapOf<FilterableAttribute, FilterState>()
        private set

    // State for search query using StateFlow (recommended)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- OR --- simple mutableState if you prefer (less ideal for observing from UI in some cases)
    // var searchQuery by mutableStateOf("")
    //    private set


    init {
        loadAllTasks()
        applySortingAndFiltering() // Initial load
    }

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) { // if (_searchQuery.value != query) for StateFlow
            _searchQuery.value = query      // if (searchQuery != query) for simple mutableState
            // searchQuery = query          // searchQuery = query for simple mutableState
            applySortingAndFiltering()
        }
    }

    private fun loadAllTasks() {
        val loadedTasks = prefsManager.taskItems
        _allTaskItems.clear()
        _allTaskItems.addAll(loadedTasks)
        currentTaskId = if (loadedTasks.isNotEmpty()) {
            (loadedTasks.maxOfOrNull { it.id } ?: 0) + 1
        } else {
            1
        }
    }

    private fun saveAllTasks() {
        prefsManager.taskItems = _allTaskItems.toList()
    }

    private fun applySortingAndFiltering() {
        _displayedTaskItems.clear()
        var tasksToProcess = if (currentSelectedListId != null) {
            _allTaskItems.filter { it.listId == currentSelectedListId }
        } else {
            emptyList()
        }

        // Apply search filter first (if query is not blank)
        val currentQuery = searchQuery.value // Use .value for StateFlow
        // val currentQuery = searchQuery // Use directly for simple mutableState

        if (currentQuery.isNotBlank()) {
            tasksToProcess = tasksToProcess.filter { task ->
                task.task.contains(currentQuery, ignoreCase = true) ||
                        (task.description?.contains(currentQuery, ignoreCase = true) == true)
                // Add other fields to search if needed (e.g., priority.name)
            }
        }


        if (filterStates.isNotEmpty()) {
            tasksToProcess = tasksToProcess.filter { task ->
                val includedFilters = filterStates.filterValues { it == FilterState.INCLUDED }.keys
                val matchesIncluded = if (includedFilters.isNotEmpty()) {
                    includedFilters.any { attribute -> task.matchesAttribute(attribute) }
                } else {
                    true
                }

                val excludedFilters = filterStates.filterValues { it == FilterState.EXCLUDED }.keys
                val matchesExcluded = excludedFilters.none { attribute -> task.matchesAttribute(attribute) }

                matchesIncluded && matchesExcluded
            }
        }

        tasksToProcess = sortTasks(tasksToProcess, currentSortOption, currentSortOrder)
        _displayedTaskItems.addAll(tasksToProcess)
    }

    private fun TaskItem.matchesAttribute(attribute: FilterableAttribute): Boolean {
        return when (attribute) {
            FilterableAttribute.HAS_DESCRIPTION -> this.description?.isNotBlank() == true
            FilterableAttribute.IS_LOW_PRIORITY -> this.priority == Priority.LOW
            FilterableAttribute.IS_HIGH_PRIORITY -> this.priority == Priority.HIGH
            FilterableAttribute.IS_HIGHEST_PRIORITY -> this.priority == Priority.HIGHEST
            FilterableAttribute.HAS_DUE_DATE -> this.dueDateMillis != null
            FilterableAttribute.HAS_DUE_TIME -> this.dueTimeHour != null && this.dueTimeMinute != null
        }
    }

    private fun sortTasks(
        tasks: List<TaskItem>,
        option: SortOption,
        order: SortOrder,
    ): List<TaskItem> {
        val comparator: Comparator<TaskItem> = when (option) {
            SortOption.FREE_SORTING -> compareBy { it.displayOrder }
            SortOption.CREATION_DATE -> compareBy { it.creationTimestamp }
            SortOption.DUE_DATE -> compareByDescending<TaskItem> { it.dueDateMillis == null }.thenBy { it.dueDateMillis }
            SortOption.COMPLETENESS -> compareBy { it.isCompleted }
            SortOption.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.task }
            SortOption.IMPORTANCE -> compareByDescending { it.priority.ordinal }
        }

        return if (order == SortOrder.ASCENDING) {
            tasks.sortedWith(comparator)
        } else {
            tasks.sortedWith(comparator.reversed())
        }
    }

    fun setSortCriteria(option: SortOption, order: SortOrder) {
        if (currentSortOption != option || currentSortOrder != order) {
            currentSortOption = option
            currentSortOrder = order
            applySortingAndFiltering()
        }
    }

    fun updateMultipleFilterStates(newStates: Map<FilterableAttribute, FilterState>) {
        var changed = false

        val attributesToRemove = filterStates.keys.filterNot { it in newStates.keys }
        attributesToRemove.forEach { attribute ->
            if (filterStates.remove(attribute) != null) {
                changed = true
            }
        }

        newStates.forEach { (attribute, newState) ->
            if (filterStates[attribute] != newState) {
                filterStates[attribute] = newState
                changed = true
            }
        }

        if (changed) {
            applySortingAndFiltering()
        }
    }

    fun resetAllFilters() {
        if (filterStates.isNotEmpty()) {
            filterStates.clear()
            applySortingAndFiltering()
        }
    }
    private fun determineNextDisplayOrder(forListId: String): Int {
        return _allTaskItems
            .filter { it.listId == forListId }
            .maxOfOrNull { it.displayOrder }?.plus(1) ?: 0
    }

    fun addItem(
        task: String,
        description: String? = null,
        priority: Priority = Priority.LOW,
        dueDateMillis: Long? = null,
        dueTimeHour: Int? = null,
        dueTimeMinute: Int? = null,
    ) {
        val listIdForNewTask = currentSelectedListId
        if (task.isNotBlank() && listIdForNewTask != null) {
            val newItem = TaskItem(
                id = currentTaskId++,
                task = task.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                priority = priority,
                isCompleted = false,
                listId = listIdForNewTask,
                dueDateMillis = dueDateMillis,
                dueTimeHour = dueTimeHour,
                dueTimeMinute = dueTimeMinute,
                creationTimestamp = System.currentTimeMillis(),
                displayOrder = determineNextDisplayOrder(listIdForNewTask)
            )
            _allTaskItems.add(newItem)
            saveAllTasks()
            applySortingAndFiltering()
        } else if (listIdForNewTask == null) {
            System.err.println("Cannot add task: No list selected.")
        }
    }

    fun removeItem(itemId: Int) {
        val itemToRemove = _allTaskItems.find { it.id == itemId }
        val removed = _allTaskItems.removeAll { it.id == itemId }
        if (removed && itemToRemove != null) {
            saveAllTasks()
            applySortingAndFiltering()
        }
    }

    fun toggleCompleted(itemId: Int) {
        val indexInAll = _allTaskItems.indexOfFirst { it.id == itemId }
        if (indexInAll != -1) {
            val oldItem = _allTaskItems[indexInAll]
            _allTaskItems[indexInAll] = oldItem.copy(isCompleted = !oldItem.isCompleted)
            saveAllTasks()
            applySortingAndFiltering()
        }
    }

    fun updateItem(
        updatedItem: TaskItem,
    ) {
        val indexInAll = _allTaskItems.indexOfFirst { it.id == updatedItem.id }
        if (indexInAll != -1) {
            val currentItem = _allTaskItems[indexInAll]
            _allTaskItems[indexInAll] = updatedItem.copy(
                listId = currentItem.listId,
                creationTimestamp = currentItem.creationTimestamp,
                displayOrder = currentItem.displayOrder
            )
            saveAllTasks()
            applySortingAndFiltering()
        }
    }
    fun clearTasksForList(listIdToClear: String) {
        val tasksWereRemoved = _allTaskItems.removeAll { it.listId == listIdToClear }
        if (tasksWereRemoved) {
            saveAllTasks()
            applySortingAndFiltering()
        }
    }


    fun moveItemInFreeSort(itemIdToMove: Int, newDisplayOrderCandidate: Int) {
        if (currentSortOption != SortOption.FREE_SORTING || currentSelectedListId == null) {
            System.err.println("Manual reordering only available in FREE_SORTING mode for a selected list.")
            return
        }

        val listId = currentSelectedListId ?: return
        val itemsInList = _allTaskItems.filter { it.listId == listId }.sortedBy { it.displayOrder }.toMutableList()

        val itemToMoveIndex = itemsInList.indexOfFirst { it.id == itemIdToMove }
        if (itemToMoveIndex == -1) {
            System.err.println("Item to move not found in the current list.")
            return
        }

        val item = itemsInList.removeAt(itemToMoveIndex)
        val targetIndex = newDisplayOrderCandidate.coerceIn(0, itemsInList.size)
        itemsInList.add(targetIndex, item)

        itemsInList.forEachIndexed { newOrder, taskItem ->
            val originalTaskIndexInAll = _allTaskItems.indexOfFirst { it.id == taskItem.id }
            if (originalTaskIndexInAll != -1) {
                if (_allTaskItems[originalTaskIndexInAll].displayOrder != newOrder) {
                    _allTaskItems[originalTaskIndexInAll] = _allTaskItems[originalTaskIndexInAll].copy(displayOrder = newOrder)
                }
            }
        }

        saveAllTasks()
        applySortingAndFiltering()
    }


    companion object {
        const val DEFAULT_LIST_ID = "default_list"
    }
}
