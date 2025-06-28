package com.xenon.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.xenon.todolist.SharedPreferenceManager
import com.xenon.todolist.viewmodel.classes.Priority
import com.xenon.todolist.viewmodel.classes.TaskItem

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
                filterTasksForDisplay()
            }
        }

    init {
        loadAllTasks()
        filterTasksForDisplay()
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

    private fun filterTasksForDisplay() {
        _displayedTaskItems.clear()
        if (currentSelectedListId != null) {
            _displayedTaskItems.addAll(_allTaskItems.filter { it.listId == currentSelectedListId })
        }

    }

    fun addItem(
        task: String,
        description: String? = null,
        priority: Priority = Priority.LOW
    ) {
        val listIdForNewTask = currentSelectedListId
        if (task.isNotBlank() && listIdForNewTask != null) {
            val newItem = TaskItem(
                id = currentTaskId++,
                task = task.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                priority = priority,
                isCompleted = false,
                listId = listIdForNewTask
            )
            _allTaskItems.add(newItem)
            saveAllTasks()
            filterTasksForDisplay()
        } else if (listIdForNewTask == null) {
            System.err.println("Cannot add task: No list selected.")
        }
    }

    fun removeItem(itemId: Int) {
        val removed = _allTaskItems.removeAll { it.id == itemId }
        if (removed) {
            saveAllTasks()
            filterTasksForDisplay()
        }
    }

    fun toggleCompleted(itemId: Int) {
        val indexInAll = _allTaskItems.indexOfFirst { it.id == itemId }
        if (indexInAll != -1) {
            val oldItem = _allTaskItems[indexInAll]
            _allTaskItems[indexInAll] = oldItem.copy(isCompleted = !oldItem.isCompleted)
            saveAllTasks()
            val indexInDisplayed = _displayedTaskItems.indexOfFirst { it.id == itemId }
            if (indexInDisplayed != -1) {
                _displayedTaskItems[indexInDisplayed] = _allTaskItems[indexInAll]
            }
        }
    }

    fun updateItem(updatedItem: TaskItem) {
        val indexInAll = _allTaskItems.indexOfFirst { it.id == updatedItem.id }
        if (indexInAll != -1) {
            val currentItem = _allTaskItems[indexInAll]
            if (updatedItem.task.isNotBlank()) {
                _allTaskItems[indexInAll] = updatedItem.copy(listId = currentItem.listId)
                saveAllTasks()
                val indexInDisplayed = _displayedTaskItems.indexOfFirst { it.id == updatedItem.id }
                if (indexInDisplayed != -1) {
                    _displayedTaskItems[indexInDisplayed] = _allTaskItems[indexInAll]
                }
            }
        }
    }

    fun clearTasksForList(listIdToClear: String) {
        val tasksWereRemoved = _allTaskItems.removeAll { it.listId == listIdToClear }
        if (tasksWereRemoved) {
            saveAllTasks()
            if (currentSelectedListId == listIdToClear) {
                filterTasksForDisplay()
            }
        }
    }
}