package com.xenonware.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xenonware.todolist.data.SharedPreferenceManager
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskItem
import com.xenonware.todolist.viewmodel.classes.TaskStep
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


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

sealed class SnackbarEvent {
    data class ShowUndoDeleteSnackbar(val taskItem: TaskItem) : SnackbarEvent()
}


class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)
    private val _allTaskItems = mutableStateListOf<TaskItem>()
    private val _displayedTaskItems = mutableStateListOf<Any>()
    val taskItems: List<Any> get() = _displayedTaskItems
    private var currentTaskId = 1

    private var recentlyDeletedItem: TaskItem? = null
    private var recentlyDeletedItemOriginalIndex: Int = -1


    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent: SharedFlow<SnackbarEvent> = _snackbarEvent.asSharedFlow()


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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    init {
        loadAllTasks()
        applySortingAndFiltering()
    }

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            applySortingAndFiltering()
        }
    }

    private fun loadAllTasks() {
        currentSortOption = prefsManager.sortOption
        currentSortOrder = prefsManager.sortOrder
        val loadedTasks = prefsManager.taskItems
        _allTaskItems.clear()
        _allTaskItems.addAll(loadedTasks)
        currentTaskId = if (loadedTasks.isNotEmpty()) {
            (loadedTasks.maxOfOrNull { it.id } ?: 0) + 1
        } else {
            1
        }
    }

    fun saveAllTasks() {
        prefsManager.taskItems = _allTaskItems.toList()
    }

    private fun applySortingAndFiltering(preserveRecentlyDeleted: Boolean = false) {
        val currentRecentlyDeleted = if (preserveRecentlyDeleted) recentlyDeletedItem else null
        val tempAllTaskItems = _allTaskItems.toMutableList()
        if (currentRecentlyDeleted != null && !tempAllTaskItems.contains(currentRecentlyDeleted)) {
        }


        _displayedTaskItems.clear()
        var tasksToProcess = if (currentSelectedListId != null) {
            _allTaskItems.filter { it.listId == currentSelectedListId }
        } else {
            emptyList()
        }

        val currentQuery = searchQuery.value
        if (currentQuery.isNotBlank()) {
            tasksToProcess = tasksToProcess.filter { task ->
                task.task.contains(currentQuery, ignoreCase = true) ||
                        (task.description?.contains(currentQuery, ignoreCase = true) == true)
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

        val sortedTasks = sortTasks(tasksToProcess, currentSortOption, currentSortOrder)

        if (currentSortOption != SortOption.FREE_SORTING && sortedTasks.isNotEmpty()) {
            val groupedItems = mutableListOf<Any>()
            var lastHeader: String? = null

            for (task in sortedTasks) {
                task.currentHeader = getHeaderForTask(task, currentSortOption, currentSortOrder)
                if (task.currentHeader != lastHeader) {
                    groupedItems.add(task.currentHeader)
                    lastHeader = task.currentHeader
                }
                groupedItems.add(task)
            }
            _displayedTaskItems.addAll(groupedItems)
        } else {
            for (task in sortedTasks) {
                task.currentHeader = ""
            }
            _displayedTaskItems.addAll(sortedTasks)
        }
    }

    private fun getHeaderForTask(task: TaskItem, sortOption: SortOption, sortOrder: SortOrder): String {
        return when (sortOption) {
            SortOption.COMPLETENESS -> if (task.isCompleted) "Completed" else "Not Completed"
            SortOption.IMPORTANCE -> "Importance: ${task.priority.name.lowercase().replaceFirstChar { it.titlecase() }}"
            SortOption.DUE_DATE -> {
                if (task.dueDateMillis == null) {
                    "No Due Date"
                } else {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(task.dueDateMillis))
                }
            }
            SortOption.CREATION_DATE -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(task.creationTimestamp))
            }
            SortOption.NAME -> {
                task.task.firstOrNull()?.uppercaseChar()?.toString() ?: "Unknown"
            }
            SortOption.FREE_SORTING -> ""
        }
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
            SortOption.CREATION_DATE -> compareBy<TaskItem> { it.creationTimestamp }.thenBy { it.displayOrder }
            SortOption.DUE_DATE -> compareByDescending<TaskItem> { it.dueDateMillis == null }
                .thenBy { it.dueDateMillis }
                .thenBy { it.displayOrder }
            SortOption.COMPLETENESS -> compareBy<TaskItem> { it.isCompleted }
                .thenBy { it.displayOrder }
            SortOption.NAME -> compareBy<TaskItem, String>(String.CASE_INSENSITIVE_ORDER) { it.task }
                .thenBy { it.displayOrder }
            SortOption.IMPORTANCE -> compareByDescending<TaskItem> { it.priority.ordinal }
                .thenBy { it.displayOrder }
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
            prefsManager.sortOption = option
            prefsManager.sortOrder = order
            applySortingAndFiltering()
        }
    }

    fun swapDisplayOrder(from: Int, to: Int) {
        val item1 = taskItems[from] as? TaskItem
        val item2 = taskItems[to] as? TaskItem
        if (item1 != null && item2 != null) {
            val tmp = item1.displayOrder
            item1.displayOrder = item2.displayOrder
            item2.displayOrder = tmp
        }
        _displayedTaskItems.add(to, _displayedTaskItems.removeAt(from))
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
        steps: List<TaskStep> = emptyList()
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
                displayOrder = determineNextDisplayOrder(listIdForNewTask),
                steps = steps
            )
            _allTaskItems.add(newItem)
            saveAllTasks()
            applySortingAndFiltering()
        } else if (listIdForNewTask == null) {
            System.err.println("Cannot add task: No list selected.")
        }
    }

    fun prepareRemoveItem(itemId: Int) {
        val itemIndex = _allTaskItems.indexOfFirst { it.id == itemId }
        if (itemIndex != -1) {
            recentlyDeletedItem = _allTaskItems[itemIndex]
            recentlyDeletedItemOriginalIndex = itemIndex

            _allTaskItems.removeAt(itemIndex)
            applySortingAndFiltering(preserveRecentlyDeleted = true)

            viewModelScope.launch {
                recentlyDeletedItem?.let {
                    _snackbarEvent.emit(SnackbarEvent.ShowUndoDeleteSnackbar(it))
                }
            }
        }
    }

    fun undoRemoveItem() {
        recentlyDeletedItem?.let { itemToRestore ->
            if (recentlyDeletedItemOriginalIndex != -1 && recentlyDeletedItemOriginalIndex <= _allTaskItems.size) {
                _allTaskItems.add(recentlyDeletedItemOriginalIndex, itemToRestore)
            } else {
                _allTaskItems.add(itemToRestore)
            }
            recentlyDeletedItem = null
            recentlyDeletedItemOriginalIndex = -1
            applySortingAndFiltering()
        }
    }

    fun confirmRemoveItem() {
        if (recentlyDeletedItem != null) {
            saveAllTasks()
            recentlyDeletedItem = null
            recentlyDeletedItemOriginalIndex = -1
        }
    }


    fun toggleCompleted(itemId: Int) {
        val indexInAll = _allTaskItems.indexOfFirst { it.id == itemId }
        if (indexInAll != -1) {
            val oldItem = _allTaskItems[indexInAll]
            oldItem.isCompleted = !oldItem.isCompleted
            _allTaskItems[indexInAll] = oldItem
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
        if (recentlyDeletedItem?.listId == listIdToClear) {
            recentlyDeletedItem = null
            recentlyDeletedItemOriginalIndex = -1
        }
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

    fun addStepToTask(taskId: Int, stepText: String) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1 && stepText.isNotBlank()) {
            val task = _allTaskItems[taskIndex]
            val newStep = TaskStep(
                id = UUID.randomUUID().toString(),
                text = stepText.trim(),
                isCompleted = false,
                displayOrder = task.steps.size
            )
            val updatedSteps = task.steps + newStep
            _allTaskItems[taskIndex] = task.copy(steps = updatedSteps)
            saveAllTasks()
            applySortingAndFiltering()
        }
    }

    fun toggleStepCompletion(taskId: Int, stepId: String) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = _allTaskItems[taskIndex]
            val stepIndex = task.steps.indexOfFirst { it.id == stepId }
            if (stepIndex != -1) {
                val step = task.steps[stepIndex]
                val updatedStep = step.copy(isCompleted = !step.isCompleted)
                val updatedSteps = task.steps.toMutableList()
                updatedSteps[stepIndex] = updatedStep
                _allTaskItems[taskIndex] = task.copy(steps = updatedSteps)
                saveAllTasks()
                applySortingAndFiltering()
            }
        }
    }

    fun removeStepFromTask(taskId: Int, stepId: String) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = _allTaskItems[taskIndex]
            val updatedSteps = task.steps.filterNot { it.id == stepId }
            if (updatedSteps.size != task.steps.size) {
                _allTaskItems[taskIndex] = task.copy(steps = updatedSteps)
                saveAllTasks()
                applySortingAndFiltering()
            }
        }
    }

    fun updateStepInTask(taskId: Int, updatedStep: TaskStep) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = _allTaskItems[taskIndex]
            val stepIndex = task.steps.indexOfFirst { it.id == updatedStep.id }
            if (stepIndex != -1) {
                val updatedSteps = task.steps.toMutableList()
                updatedSteps[stepIndex] = updatedStep
                _allTaskItems[taskIndex] = task.copy(steps = updatedSteps)
                saveAllTasks()
                applySortingAndFiltering()
            }
        }
    }


    companion object {
        const val DEFAULT_LIST_ID = "default_list"
    }
}
