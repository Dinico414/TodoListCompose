package com.xenon.todolist.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.xenon.todolist.viewmodel.classes.TodoItem

class TodoViewModel : ViewModel() {
    private val _todoItems = mutableStateListOf<TodoItem>()
    val todoItems: List<TodoItem> get() = _todoItems

    private var currentId = 1

    fun addItem(task: String) {
        if (task.isNotBlank()) {
            val newItem = TodoItem(id = currentId++, task = task, isCompleted = false)
            _todoItems.add(newItem)
            Log.d("TodoViewModel", "addItem: Added ${newItem.id}, list size: ${_todoItems.size}")
        }
    }

    // Modify removeItem to find by ID
    fun removeItem(itemId: Int) {
        val itemToRemove = _todoItems.find { it.id == itemId }
        if (itemToRemove != null) {
            _todoItems.remove(itemToRemove)
            Log.d("TodoViewModel", "removeItem: Removed $itemId, list size: ${_todoItems.size}")
        } else {
            Log.w("TodoViewModel", "removeItem: Item ID $itemId not found!")
        }
    }

    // Modify toggleCompleted to find by ID
    fun toggleCompleted(itemId: Int) {
        val index = _todoItems.indexOfFirst { it.id == itemId }
        Log.d("TodoViewModel", "toggleCompleted for ID $itemId. Found Index: $index.")
        if (index != -1) {
            val oldItem = _todoItems[index]
            _todoItems[index] = oldItem.copy(isCompleted = !oldItem.isCompleted)
            Log.d("TodoViewModel", "ID $itemId toggled. Old state: ${oldItem.isCompleted}, New state: ${_todoItems[index].isCompleted}")
        } else {
            Log.w("TodoViewModel", "toggleCompleted: Item ID $itemId not found in list for toggling!")
        }
    }
}