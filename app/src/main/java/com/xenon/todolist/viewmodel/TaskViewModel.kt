package com.xenon.todolist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.xenon.todolist.viewmodel.classes.TaskItem

class TaskViewModel : ViewModel() {
    private val _taskItems = mutableStateListOf<TaskItem>()
    val taskItems: List<TaskItem> get() = _taskItems

    private var currentId = 1

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
        }
    }
    fun removeItem(itemId: Int) {
        _taskItems.removeAll { it.id == itemId }
    }

    fun toggleCompleted(itemId: Int) {
        val index = _taskItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val oldItem = _taskItems[index]
            _taskItems[index] = oldItem.copy(isCompleted = !oldItem.isCompleted)
        }
    }

    fun updateItem(updatedItem: TaskItem) {
        val index = _taskItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            if (updatedItem.task.isNotBlank()) {
                _taskItems[index] = updatedItem
            }
        }
    }
}