package com.xenonware.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.xenon.todolist.R
import com.xenonware.todolist.SharedPreferenceManager
import com.xenonware.todolist.viewmodel.classes.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

const val DEFAULT_LIST_ID = "default_my_tasks_list_id"

class TodoViewModel(
    application: Application,
    private val taskViewModel: TaskViewModel
) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)
    private val resources = application.resources

    private val defaultListName: String = resources.getString(R.string.my_tasklist)

    val drawerItems = mutableStateListOf<TodoItem>()

    private val _selectedDrawerItemId = mutableStateOf(DEFAULT_LIST_ID)
    val selectedDrawerItemId: State<String> = _selectedDrawerItemId

    var isDrawerSelectionModeActive by mutableStateOf(false)
        private set

    var showAddListDialog by mutableStateOf(false)
    var showRenameListDialog by mutableStateOf(false)
    var itemToRenameId by mutableStateOf<String?>(null)
    var itemToRenameCurrentName by mutableStateOf("")
    var showConfirmDeleteDialog by mutableStateOf(false)

    val drawerOpenFlow = MutableStateFlow<Boolean>(false)

    init {
        loadDrawerItems()
    }

    private fun loadDrawerItems() {
        val loadedItems = prefsManager.drawerTodoItems.toMutableList()
        if (loadedItems.none { it.id == DEFAULT_LIST_ID }) {
            loadedItems.add(0,
                TodoItem(id = DEFAULT_LIST_ID, title = defaultListName, isSelectedForAction = false)
            )
            prefsManager.drawerTodoItems = loadedItems
        }
        drawerItems.clear()
        drawerItems.addAll(loadedItems)
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }

        if (drawerItems.none { it.id == _selectedDrawerItemId.value }) {
            _selectedDrawerItemId.value = DEFAULT_LIST_ID
        }
        isDrawerSelectionModeActive = false
    }

    fun saveDrawerItems() {
        prefsManager.drawerTodoItems = drawerItems.toList()
    }

    fun onDrawerItemClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            if (_selectedDrawerItemId.value != itemId) {
                _selectedDrawerItemId.value = itemId
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
            drawerItems.add(
                TodoItem(
                    id = newListId,
                    title = newListName.trim(),
                    isSelectedForAction = false
                )
            )
            saveDrawerItems()
            _selectedDrawerItemId.value = newListId
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
                    drawerItems[defaultListIndex] = drawerItems[defaultListIndex].copy(title = defaultListName, isSelectedForAction = false)
                }
                taskViewModel.clearTasksForList(DEFAULT_LIST_ID)
                if(_selectedDrawerItemId.value == DEFAULT_LIST_ID) selectedListWasAlteredOrRemoved = true

            } else {
                drawerItems.removeAll { it.id == item.id }
                taskViewModel.clearTasksForList(item.id)
                if (_selectedDrawerItemId.value == item.id) {
                    _selectedDrawerItemId.value = DEFAULT_LIST_ID
                    selectedListWasAlteredOrRemoved = true
                }
            }
        }

        saveDrawerItems()

        if (selectedListWasAlteredOrRemoved) {
            if (drawerItems.none { it.id == _selectedDrawerItemId.value }) {
                _selectedDrawerItemId.value = drawerItems.firstOrNull { it.id == DEFAULT_LIST_ID }?.id ?: drawerItems.firstOrNull()?.id ?: ""
            }
        }
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }
        isDrawerSelectionModeActive = false
        closeConfirmDeleteDialog()
    }
}