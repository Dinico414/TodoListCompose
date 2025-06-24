package com.xenon.todolist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.xenon.todolist.viewmodel.classes.TodoItem

class TaskViewModel : ViewModel() {
    private val _todoItems = mutableStateListOf<TodoItem>()
    val todoItems: List<TodoItem> get() = _todoItems

    private var currentId = 1

    fun addItem(task: String) {
        if (task.isNotBlank()) {
            val newItem = TodoItem(id = currentId++, task = task, isCompleted = false)
            _todoItems.add(newItem)
        }
    }

    // Modify removeItem to find by ID
    fun removeItem(itemId: Int) {
        val itemToRemove = _todoItems.find { it.id == itemId }
        if (itemToRemove != null) {
            _todoItems.remove(itemToRemove)
        }
    }

    // Modify toggleCompleted to find by ID
    fun toggleCompleted(itemId: Int) {
        val index = _todoItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val oldItem = _todoItems[index]
            _todoItems[index] = oldItem.copy(isCompleted = !oldItem.isCompleted)
        }
    }
}