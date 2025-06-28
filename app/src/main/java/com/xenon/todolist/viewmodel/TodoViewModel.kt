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

    // Dialog States
    var showAddListDialog by mutableStateOf(false)
        private set
    var showRenameListDialog by mutableStateOf(false)
        private set
    var itemToRenameId by mutableStateOf<String?>(null)
        private set
    var itemToRenameCurrentName by mutableStateOf("")
        private set
    var showConfirmDeleteDialog by mutableStateOf(false)
        private set


    fun onDrawerItemClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            selectedDrawerItemId = itemId
        }
    }

    // --- Add List Dialog ---
    fun openAddListDialog() {
        showAddListDialog = true
    }

    fun closeAddListDialog() {
        showAddListDialog = false
    }

    fun onConfirmAddNewList(newListName: String) {
        val newListId = UUID.randomUUID().toString()
        drawerItems.add(TodoItem(id = newListId, title = newListName))
        isDrawerSelectionModeActive = false // Should not be needed if adding new list
        closeAddListDialog()
    }

    // --- Rename List Dialog ---
    fun openRenameListDialog(itemId: String, currentName: String) {
        itemToRenameId = itemId
        itemToRenameCurrentName = currentName
        showRenameListDialog = true
    }

    fun closeRenameListDialog() {
        showRenameListDialog = false
        itemToRenameId = null
        itemToRenameCurrentName = ""
    }

    fun onConfirmRenameList(newName: String) {
        itemToRenameId?.let { id ->
            renameItem(id, newName)
        }
        closeRenameListDialog()
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

    // --- Confirm Delete Dialog ---
    fun openConfirmDeleteDialog() {
        showConfirmDeleteDialog = true
    }

    fun closeConfirmDeleteDialog() {
        showConfirmDeleteDialog = false
    }

    fun onConfirmDeleteSelected() {
        drawerItems.removeAll { it.isSelectedForAction }
        isDrawerSelectionModeActive = false
        closeConfirmDeleteDialog()
    }

    private fun toggleItemSelectionForAction(itemId: String) {
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = drawerItems[index]
            drawerItems[index] = item.copy(isSelectedForAction = !item.isSelectedForAction)
        }
    }

    private fun renameItem(itemId: String, newName: String) { // Made private as it's called internally
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            drawerItems[index] = drawerItems[index].copy(title = newName)
        }
    }
}