package com.xenon.todolist.viewmodel

import android.app.Application // Required for Application context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel // Use AndroidViewModel for context
import com.xenon.todolist.SharedPreferenceManager
import com.xenon.todolist.viewmodel.classes.Priority // Ensure this is your Priority enum
import com.xenon.todolist.viewmodel.classes.TaskItem

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)
    private val _taskItems = mutableStateListOf<TaskItem>()
    val taskItems: List<TaskItem> get() = _taskItems

    private var currentId = 1

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val loadedTasks = prefsManager.taskItems
        _taskItems.addAll(loadedTasks)
        if (loadedTasks.isNotEmpty()) {
            currentId = (loadedTasks.maxOfOrNull { it.id } ?: 0) + 1
        }
    }

    private fun saveTasks() {
        prefsManager.taskItems = _taskItems.toList()
    }

    fun addItem(
        task: String,
        description: String? = null,
        priority: Priority = Priority.LOW
    ) {
        if (task.isNotBlank()) {
            val newItem = TaskItem(
                id = currentId++,
                task = task.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                priority = priority,
                isCompleted = false
            )
            _taskItems.add(newItem)
            saveTasks()
        }
    }

    fun removeItem(itemId: Int) {
        val removed = _taskItems.removeAll { it.id == itemId }
        if (removed) {
            saveTasks()
        }
    }

    fun toggleCompleted(itemId: Int) {
        val index = _taskItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val oldItem = _taskItems[index]
            _taskItems[index] = oldItem.copy(isCompleted = !oldItem.isCompleted)
            saveTasks()
        }
    }

    fun updateItem(updatedItem: TaskItem) {
        val index = _taskItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            if (updatedItem.task.isNotBlank()) {
                _taskItems[index] = updatedItem
                saveTasks()
            }
        }
    }
}