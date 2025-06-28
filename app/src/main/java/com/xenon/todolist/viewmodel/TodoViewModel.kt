package com.xenon.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.State // Import State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.xenon.todolist.SharedPreferenceManager
import com.xenon.todolist.viewmodel.classes.TodoItem
import java.util.UUID

const val DEFAULT_LIST_ID = "default_my_tasks_list_id"
const val DEFAULT_LIST_NAME = "My Tasks"

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)

    val drawerItems = mutableStateListOf<TodoItem>()

    // Private MutableState for internal updates
    private val _selectedDrawerItemId = mutableStateOf(DEFAULT_LIST_ID)
    // Public immutable State for observation by the UI
    val selectedDrawerItemId: State<String> = _selectedDrawerItemId // Expose as State<String>

    var isDrawerSelectionModeActive by mutableStateOf(false)
        private set

    var showAddListDialog by mutableStateOf(false)
    var showRenameListDialog by mutableStateOf(false)
    var itemToRenameId by mutableStateOf<String?>(null)
    var itemToRenameCurrentName by mutableStateOf("")
    var showConfirmDeleteDialog by mutableStateOf(false)

    init {
        loadDrawerItems()
    }

    private fun loadDrawerItems() {
        val loadedItems = prefsManager.drawerTodoItems.toMutableList()
        if (loadedItems.none { it.id == DEFAULT_LIST_ID }) {
            loadedItems.add(0, TodoItem(id = DEFAULT_LIST_ID, title = DEFAULT_LIST_NAME, isSelectedForAction = false))
            prefsManager.drawerTodoItems = loadedItems
        }
        drawerItems.clear()
        drawerItems.addAll(loadedItems)
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }

        // Update the internal _selectedDrawerItemId
        if (drawerItems.none { it.id == _selectedDrawerItemId.value }) {
            _selectedDrawerItemId.value = DEFAULT_LIST_ID
        }
        isDrawerSelectionModeActive = false
    }

    private fun saveDrawerItems() {
        prefsManager.drawerTodoItems = drawerItems.toList()
    }

    fun onDrawerItemClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            if (_selectedDrawerItemId.value != itemId) {
                _selectedDrawerItemId.value = itemId // Update the internal MutableState
                drawerItems.replaceAll { it.copy(isSelectedForAction = false) }
                isDrawerSelectionModeActive = false
            }
        } else {
            val index = drawerItems.indexOfFirst { it.id == itemId }
            if (index != -1) {
                val item = drawerItems[index]
                drawerItems[index] = item.copy(isSelectedForAction = !item.isSelectedForAction)
                if (drawerItems.none { it.isSelectedForAction }) {
                    isDrawerSelectionModeActive = false
                }
            }
        }
    }

    // onItemLongClick and onItemCheckedChanged remain the same as they modify drawerItems, not selectedDrawerItemId directly for navigation

    fun onItemLongClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            isDrawerSelectionModeActive = true
        }
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = drawerItems[index]
            drawerItems[index] = item.copy(isSelectedForAction = !item.isSelectedForAction)

            if (drawerItems.none { it.isSelectedForAction }) {
                isDrawerSelectionModeActive = false
            }
        }
    }

    fun onItemCheckedChanged(itemId: String, isChecked: Boolean) {
        val index = drawerItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            drawerItems[index] = drawerItems[index].copy(isSelectedForAction = isChecked)
            if (!isChecked && drawerItems.none { it.isSelectedForAction }) {
                isDrawerSelectionModeActive = false
            } else if (isChecked) {
                isDrawerSelectionModeActive = true
            }
        }
    }


    fun clearAllSelections() {
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }
        isDrawerSelectionModeActive = false
    }

    fun openAddListDialog() {
        showAddListDialog = true
    }

    fun closeAddListDialog() {
        showAddListDialog = false
    }

    fun onConfirmAddNewList(newListName: String) {
        if (newListName.isNotBlank()) {
            val newListId = UUID.randomUUID().toString()
            drawerItems.add(TodoItem(id = newListId, title = newListName.trim(), isSelectedForAction = false))
            saveDrawerItems()
            _selectedDrawerItemId.value = newListId // Update internal state
            isDrawerSelectionModeActive = false
            drawerItems.replaceAll { it.copy(isSelectedForAction = false) }
        }
        closeAddListDialog()
    }

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
        if (newName.isNotBlank()) {
            itemToRenameId?.let { idToRename ->
                val index = drawerItems.indexOfFirst { it.id == idToRename }
                if (index != -1) {
                    drawerItems[index] = drawerItems[index].copy(title = newName.trim())
                    saveDrawerItems()
                }
            }
        }
        closeRenameListDialog()
        itemToRenameId?.let { deselectedId ->
            val index = drawerItems.indexOfFirst { it.id == deselectedId }
            if (index != -1 && drawerItems[index].isSelectedForAction) {
                drawerItems[index] = drawerItems[index].copy(isSelectedForAction = false)
                if (drawerItems.none { it.isSelectedForAction }) {
                    isDrawerSelectionModeActive = false
                }
            }
        }
    }

    fun openConfirmDeleteDialog() {
        if (drawerItems.any { it.isSelectedForAction }) {
            showConfirmDeleteDialog = true
        }
    }

    fun closeConfirmDeleteDialog() {
        showConfirmDeleteDialog = false
    }

    fun onConfirmDeleteSelected() {
        val itemsToProcess = drawerItems.filter { it.isSelectedForAction }.toList()
        var selectedListWasAlteredOrRemoved = false

        itemsToProcess.forEach { item ->
            if (item.id == DEFAULT_LIST_ID) {
                val defaultListIndex = drawerItems.indexOfFirst { it.id == DEFAULT_LIST_ID }
                if (defaultListIndex != -1) {
                    drawerItems[defaultListIndex] = drawerItems[defaultListIndex].copy(title = DEFAULT_LIST_NAME, isSelectedForAction = false)
                }
                getApplication<Application>().let { app ->
                    val taskViewModel = TaskViewModel(app) // Consider injecting TaskViewModel or using an interface
                    taskViewModel.clearTasksForList(DEFAULT_LIST_ID)
                }
                if(_selectedDrawerItemId.value == DEFAULT_LIST_ID) selectedListWasAlteredOrRemoved = true

            } else {
                drawerItems.removeAll { it.id == item.id }
                getApplication<Application>().let { app ->
                    val taskViewModel = TaskViewModel(app) // Consider injecting TaskViewModel or using an interface
                    taskViewModel.clearTasksForList(item.id)
                }
                if (_selectedDrawerItemId.value == item.id) {
                    _selectedDrawerItemId.value = DEFAULT_LIST_ID // Update internal state
                    selectedListWasAlteredOrRemoved = true
                }
            }
        }

        saveDrawerItems()

        if (selectedListWasAlteredOrRemoved) {
            if (drawerItems.none { it.id == _selectedDrawerItemId.value }) {
                _selectedDrawerItemId.value = drawerItems.firstOrNull { it.id == DEFAULT_LIST_ID }?.id ?: drawerItems.firstOrNull()?.id ?: "" // Update internal state
            }
        }
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }
        isDrawerSelectionModeActive = false
        closeConfirmDeleteDialog()
    }
}