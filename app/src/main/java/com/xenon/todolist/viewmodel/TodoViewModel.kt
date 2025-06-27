package com.xenon.todolist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.xenon.todolist.viewmodel.classes.TodoItem
import java.util.UUID

class TodoListViewModel : ViewModel() {

    val drawerItems = mutableStateListOf(
        TodoItem("default", "My Tasks"),
        TodoItem("1", "Shopping List"),
        TodoItem("2", "Todos")
    )

    var selectedDrawerItemId by mutableStateOf(drawerItems.getOrNull(1)?.id ?: drawerItems.firstOrNull()?.id ?: "")
        private set

    var isDrawerSelectionModeActive by mutableStateOf(false)
        private set

    fun onDrawerItemClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            selectedDrawerItemId = itemId
        }
    }

    fun onAddNewListClick(newListName: String = "New List ${drawerItems.size + 1}") {
        val newListId = UUID.randomUUID().toString()
        drawerItems.add(TodoItem(id = newListId, title = newListName))
        isDrawerSelectionModeActive = false
    }

    fun onItemLongClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            isDrawerSelectionModeActive = true
        }
        toggleItemSelectionForAction(itemId)
    }

    fun onItemCheckedChanged(itemId: String, isChecked: Boolean) {
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            drawerItems[index] = drawerItems[index].copy(isSelectedForAction = isChecked)
        }
        if (isDrawerSelectionModeActive && drawerItems.none { it.isSelectedForAction }) {
            isDrawerSelectionModeActive = false
        }
    }

    fun onDeleteSelectedClick() {
        drawerItems.removeAll { it.isSelectedForAction }
        isDrawerSelectionModeActive = false
    }

    private fun toggleItemSelectionForAction(itemId: String) {
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = drawerItems[index]
            drawerItems[index] = item.copy(isSelectedForAction = !item.isSelectedForAction)
        }
    }

    fun renameItem(itemId: String, newName: String) {
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            drawerItems[index] = drawerItems[index].copy(title = newName)
        }
    }

}