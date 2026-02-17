package com.xenonware.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xenonware.todolist.R
import com.xenonware.todolist.data.SharedPreferenceManager
import com.xenonware.todolist.viewmodel.classes.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

const val DEFAULT_LIST_ID = "default_my_tasks_list_id"

class TodoViewModel(
    application: Application,
    private val taskViewModel: TaskViewModel
) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)
    private val resources = application.resources
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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

    val drawerOpenFlow = MutableStateFlow(false)

    private var firestoreListener: ListenerRegistration? = null

    init {
        loadDrawerItems()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                startFirestoreListener(user.uid)
            } else {
                firestoreListener?.remove()
                firestoreListener = null
            }
        }
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

    private fun startFirestoreListener(userId: String) {
        firestoreListener?.remove()
        firestoreListener = firestore.collection("tasks").document(userId).collection("user_lists")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                for (change in snapshot.documentChanges) {
                    val item = change.document.toObject(TodoItem::class.java)
                    // Skip DEFAULT_LIST_ID from remote if we want to manage it locally, 
                    // OR treat it as just another list if we want sync.
                    // Let's allow sync for everything except if it conflicts weirdly.
                    
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            val index = drawerItems.indexOfFirst { it.id == item.id }
                            if (index == -1) {
                                // Add to end, or after default?
                                drawerItems.add(item)
                            } else {
                                // Already exists (maybe local create), update title if needed
                                // but respect local? Firestore usually wins or we merge.
                                // We'll assume remote is source of truth.
                                drawerItems[index] = drawerItems[index].copy(title = item.title)
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = drawerItems.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                drawerItems[index] = drawerItems[index].copy(title = item.title)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            // If removed remotely, remove locally
                            val index = drawerItems.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                // Handle selection change if needed
                                if (_selectedDrawerItemId.value == item.id) {
                                    _selectedDrawerItemId.value = DEFAULT_LIST_ID
                                }
                                drawerItems.removeAt(index)
                                taskViewModel.clearTasksForList(item.id)
                            }
                        }
                    }
                }
                saveDrawerItems()
            }
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
            val newItem = TodoItem(
                id = newListId,
                title = newListName.trim(),
                isSelectedForAction = false
            )
            drawerItems.add(newItem)
            saveDrawerItems()
            _selectedDrawerItemId.value = newListId
            isDrawerSelectionModeActive = false
            drawerItems.replaceAll { it.copy(isSelectedForAction = false) }

            // Sync to Firestore
            val user = auth.currentUser
            if (user != null) {
                viewModelScope.launch {
                    try {
                        firestore.collection("tasks").document(user.uid)
                            .collection("user_lists")
                            .document(newListId)
                            .set(newItem)
                            .await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
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
                    val updatedItem = drawerItems[index].copy(title = newName.trim())
                    drawerItems[index] = updatedItem
                    saveDrawerItems()

                    // Sync to Firestore
                    val user = auth.currentUser
                    if (user != null) {
                        viewModelScope.launch {
                            try {
                                firestore.collection("tasks").document(user.uid)
                                    .collection("user_lists")
                                    .document(idToRename)
                                    .set(updatedItem) // set or update
                                    .await()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
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
        val user = auth.currentUser

        itemsToProcess.forEach { item ->
            if (item.id == DEFAULT_LIST_ID) {
                val defaultListIndex = drawerItems.indexOfFirst { it.id == DEFAULT_LIST_ID }
                if (defaultListIndex != -1) {
                    val updatedDefault = drawerItems[defaultListIndex].copy(title = defaultListName, isSelectedForAction = false)
                    drawerItems[defaultListIndex] = updatedDefault
                    
                    // Sync default list rename/reset
                    if (user != null) {
                        viewModelScope.launch {
                            try {
                                firestore.collection("tasks").document(user.uid)
                                    .collection("user_lists")
                                    .document(DEFAULT_LIST_ID)
                                    .set(updatedDefault)
                                    .await()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
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

                // Sync delete
                if (user != null) {
                    viewModelScope.launch {
                        try {
                            firestore.collection("tasks").document(user.uid)
                                .collection("user_lists")
                                .document(item.id)
                                .delete()
                                .await()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
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
