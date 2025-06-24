package com.xenon.todolist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.xenon.todolist.viewmodel.classes.TodoItem

class TaskViewModel : ViewModel() {
    private val _todoItems = mutableStateListOf<TodoItem>()
    val todoItems: List<TodoItem> get() = _todoItems

    private var currentId = 1

    fun addItem(task: String, description: String? = null) {
        if (task.isNotBlank()) {
            val newItem = TodoItem(
                id = currentId++,
                task = task,
                description = description,
                isCompleted = false
            )
            _todoItems.add(newItem)
        }
    }

    fun removeItem(itemId: Int) {
        _todoItems.removeAll { it.id == itemId }
    }

    fun toggleCompleted(itemId: Int) {
        val index = _todoItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val oldItem = _todoItems[index]
            _todoItems[index] = oldItem.copy(isCompleted = !oldItem.isCompleted)
        }
    }

    fun updateItem(updatedItem: TodoItem) {
        val index = _todoItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            _todoItems[index] = updatedItem
        }
    }
}