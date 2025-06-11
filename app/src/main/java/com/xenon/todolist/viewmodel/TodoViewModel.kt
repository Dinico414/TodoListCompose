package com.xenon.todolist.viewmodel

import androidx.compose.animation.core.copy
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.xenon.todolist.viewmodel.classes.TodoItem

class TodoViewModel : ViewModel() {
    // Private mutable list to hold to-do items
    private val _todoItems = mutableStateListOf<TodoItem>()
    // Public immutable list exposed to the UI
    val todoItems: List<TodoItem> = _todoItems

    // Counter for generating unique IDs
    private var currentId = 1

    fun addItem(task: String) {
        if (task.isNotBlank()) {
            _todoItems.add(TodoItem(id = currentId++, task = task))
        }
    }

    fun removeItem(item: TodoItem) {
        _todoItems.remove(item)
    }

    fun toggleCompleted(item: TodoItem) {
        val index = _todoItems.indexOf(item)
        if (index != -1) {
            _todoItems[index] = item.copy(isCompleted = !item.isCompleted)
        }
    }
}