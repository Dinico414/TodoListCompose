package com.xenon.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.xenon.todolist.SharedPreferenceManager // Assuming this path is correct
import com.xenon.todolist.viewmodel.classes.Priority
import com.xenon.todolist.viewmodel.classes.TaskItem

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

    init {
        loadAllTasks()
        applySortingAndFiltering()
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
        var filteredTasks = if (currentSelectedListId != null) {
            _allTaskItems.filter { it.listId == currentSelectedListId }
        } else {
            emptyList()
        }

        filteredTasks = sortTasks(filteredTasks, currentSortOption, currentSortOrder)
        _displayedTaskItems.addAll(filteredTasks)
    }

    private fun sortTasks(tasks: List<TaskItem>, option: SortOption, order: SortOrder): List<TaskItem> {
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
        dueTimeMinute: Int? = null
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
        val itemsInList = _allTaskItems
            .filter { it.listId == listId }
            .sortedBy { it.displayOrder }
            .toMutableList()

        val itemToMoveIndex = itemsInList.indexOfFirst { it.id == itemIdToMove }
        if (itemToMoveIndex == -1) {
            System.err.println("Item to move not found in the current list.")
            return
        }

        val item = itemsInList.removeAt(itemToMoveIndex)
        val targetIndex = newDisplayOrderCandidate.coerceIn(0, itemsInList.size)
        itemsInList.add(targetIndex, item)

        itemsInList.forEachIndexed { index, taskItem ->
            val originalTaskIndexInAll = _allTaskItems.indexOfFirst { it.id == taskItem.id }
            if (originalTaskIndexInAll != -1) {
                _allTaskItems[originalTaskIndexInAll] = _allTaskItems[originalTaskIndexInAll].copy(displayOrder = index)
            }
        }

        saveAllTasks()
        applySortingAndFiltering()
    }

    companion object {
        const val DEFAULT_LIST_ID = "default_list"
    }
}
