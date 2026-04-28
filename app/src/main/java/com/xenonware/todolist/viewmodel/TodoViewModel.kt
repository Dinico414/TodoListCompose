package com.xenonware.todolist.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "Sync"

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

    private var currentTodoId = 1
    private val syncingTodoIds = mutableStateSetOf<String>()
    private val offlineTodoIds = mutableStateSetOf<String>()

    init {
        loadDrawerItems()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            Log.d(TAG, "TodoVM auth state changed, user=${user?.uid}")
            if (user != null) {
                onSignedIn(user.uid)
            } else {
                firestoreListener?.remove()
                firestoreListener = null
            }
        }
    }

    private fun onSignedIn(userId: String) {
        Log.d(TAG, "TodoVM.onSignedIn uid=$userId")
        startFirestoreListener(userId)
        syncListsToCloud(userId)
    }

    private fun loadDrawerItems() {
        val loadedItems = prefsManager.drawerTodoItems.toMutableList()
        if (loadedItems.none { it.id == DEFAULT_LIST_ID }) {
            loadedItems.add(0,
                TodoItem(id = DEFAULT_LIST_ID, title = defaultListName, isSelectedForAction = false, isOffline = false)
            )
            prefsManager.drawerTodoItems = loadedItems
        }
        drawerItems.clear()
        drawerItems.addAll(loadedItems)
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }

        currentTodoId = if (drawerItems.isNotEmpty()) {
            (drawerItems.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
        } else {
            1
        }

        offlineTodoIds.clear()
        drawerItems.filter { it.isOffline }.forEach { offlineTodoIds.add(it.id) }

        if (drawerItems.none { it.id == _selectedDrawerItemId.value }) {
            _selectedDrawerItemId.value = DEFAULT_LIST_ID
        }

        taskViewModel.currentSelectedListId = _selectedDrawerItemId.value
        isDrawerSelectionModeActive = false
        Log.d(TAG, "TodoVM loadDrawerItems loaded=${drawerItems.size} offline=${offlineTodoIds.size}")
    }

    private fun startFirestoreListener(userId: String) {
        Log.d(TAG, "TodoVM startFirestoreListener for uid=$userId")
        firestoreListener?.remove()
        firestoreListener = firestore.collection("tasks").document(userId).collection("user_lists")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "TodoVM listener error", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    Log.w(TAG, "TodoVM listener: null snapshot")
                    return@addSnapshotListener
                }
                Log.d(TAG, "TodoVM listener fired, ${snapshot.documentChanges.size} change(s)")

                for (change in snapshot.documentChanges) {
                    val item = try {
                        change.document.toObject(TodoItem::class.java)
                    } catch (ex: Exception) {
                        Log.e(TAG, "TodoVM failed to parse document ${change.document.id}", ex)
                        null
                    }
                    if (item == null || offlineTodoIds.contains(item.id)) continue

                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            val index = drawerItems.indexOfFirst { it.id == item.id }
                            if (index == -1) {
                                Log.d(TAG, "TodoVM remote ADD ${item.id}")
                                drawerItems.add(item.copy(isOffline = false))
                            } else {
                                drawerItems[index] = drawerItems[index].copy(title = item.title, isOffline = false)
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = drawerItems.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                Log.d(TAG, "TodoVM remote MOD ${item.id}")
                                drawerItems[index] = drawerItems[index].copy(title = item.title, isOffline = false)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            val index = drawerItems.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                Log.d(TAG, "TodoVM remote DEL ${item.id}")
                                if (_selectedDrawerItemId.value == item.id) {
                                    _selectedDrawerItemId.value = DEFAULT_LIST_ID
                                    taskViewModel.currentSelectedListId = DEFAULT_LIST_ID
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

    private fun syncListsToCloud(userId: String) {
        Log.d(TAG, "syncListsToCloud start, total items=${drawerItems.size}")
        viewModelScope.launch {
            drawerItems.toList().forEach { item ->
                Log.d(TAG, "considering list ${item.id} title='${item.title}' offline=${item.isOffline} syncing=${item.id in syncingTodoIds}")
                if (item.isOffline || item.id in syncingTodoIds) return@forEach
                syncingTodoIds.add(item.id)
                try {
                    Log.d(TAG, "uploading list ${item.id}")
                    firestore.collection("tasks").document(userId)
                        .collection("user_lists")
                        .document(item.id)
                        .set(item)
                        .await()
                    Log.d(TAG, "uploaded list ${item.id} OK")
                } catch (e: Exception) {
                    Log.e(TAG, "upload list ${item.id} FAILED", e)
                } finally {
                    syncingTodoIds.remove(item.id)
                }
            }
        }
    }

    fun saveDrawerItems() {
        prefsManager.drawerTodoItems = drawerItems.toList()
    }

    fun onDrawerItemClick(itemId: String) {
        if (!isDrawerSelectionModeActive) {
            if (_selectedDrawerItemId.value != itemId) {
                _selectedDrawerItemId.value = itemId
                taskViewModel.currentSelectedListId = itemId
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

    fun onConfirmAddNewList(newListName: String, forceLocal: Boolean = false) {
        if (newListName.isNotBlank()) {
            val newListId = (currentTodoId++).toString()
            val newItem = TodoItem(
                id = newListId,
                title = newListName.trim(),
                isSelectedForAction = false,
                isOffline = forceLocal
            )
            drawerItems.add(newItem)
            saveDrawerItems()
            _selectedDrawerItemId.value = newListId
            taskViewModel.currentSelectedListId = newListId
            isDrawerSelectionModeActive = false
            drawerItems.replaceAll { it.copy(isSelectedForAction = false) }

            if (forceLocal) {
                offlineTodoIds.add(newListId)
                Log.d(TAG, "onConfirmAddNewList $newListId stored offline-only")
            } else {
                val user = auth.currentUser
                if (user != null) {
                    syncingTodoIds.add(newListId)
                    viewModelScope.launch {
                        try {
                            Log.d(TAG, "onConfirmAddNewList $newListId pushing to Firestore")
                            firestore.collection("tasks").document(user.uid)
                                .collection("user_lists")
                                .document(newListId)
                                .set(newItem)
                                .await()
                            Log.d(TAG, "onConfirmAddNewList $newListId pushed OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "onConfirmAddNewList $newListId push FAILED", e)
                        } finally {
                            syncingTodoIds.remove(newListId)
                        }
                    }
                } else {
                    Log.d(TAG, "onConfirmAddNewList $newListId no signed-in user, kept local")
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

                    if (!updatedItem.isOffline) {
                        val user = auth.currentUser
                        if (user != null) {
                            syncingTodoIds.add(idToRename)
                            viewModelScope.launch {
                                try {
                                    Log.d(TAG, "onConfirmRenameList $idToRename pushing")
                                    firestore.collection("tasks").document(user.uid)
                                        .collection("user_lists")
                                        .document(idToRename)
                                        .set(updatedItem)
                                        .await()
                                    Log.d(TAG, "onConfirmRenameList $idToRename pushed OK")
                                } catch (e: Exception) {
                                    Log.e(TAG, "onConfirmRenameList $idToRename push FAILED", e)
                                } finally {
                                    syncingTodoIds.remove(idToRename)
                                }
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

                    if (!updatedDefault.isOffline && user != null) {
                        viewModelScope.launch {
                            try {
                                Log.d(TAG, "onConfirmDeleteSelected resetting default list")
                                firestore.collection("tasks").document(user.uid)
                                    .collection("user_lists")
                                    .document(DEFAULT_LIST_ID)
                                    .set(updatedDefault)
                                    .await()
                                Log.d(TAG, "onConfirmDeleteSelected default list reset OK")
                            } catch (e: Exception) {
                                Log.e(TAG, "onConfirmDeleteSelected default list reset FAILED", e)
                            }
                        }
                    }
                }
                taskViewModel.clearTasksForList(DEFAULT_LIST_ID)
                if(_selectedDrawerItemId.value == DEFAULT_LIST_ID) selectedListWasAlteredOrRemoved = true

            } else {
                drawerItems.removeAll { it.id == item.id }
                taskViewModel.clearTasksForList(item.id)
                offlineTodoIds.remove(item.id)
                if (_selectedDrawerItemId.value == item.id) {
                    _selectedDrawerItemId.value = DEFAULT_LIST_ID
                    taskViewModel.currentSelectedListId = DEFAULT_LIST_ID
                    selectedListWasAlteredOrRemoved = true
                }

                if (!item.isOffline && user != null) {
                    viewModelScope.launch {
                        try {
                            Log.d(TAG, "onConfirmDeleteSelected deleting list ${item.id}")
                            firestore.collection("tasks").document(user.uid)
                                .collection("user_lists")
                                .document(item.id)
                                .delete()
                                .await()
                            Log.d(TAG, "onConfirmDeleteSelected deleted list ${item.id} OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "onConfirmDeleteSelected delete list ${item.id} FAILED", e)
                        }
                    }
                }
            }
        }

        saveDrawerItems()

        if (selectedListWasAlteredOrRemoved) {
            if (drawerItems.none { it.id == _selectedDrawerItemId.value }) {
                _selectedDrawerItemId.value = drawerItems.firstOrNull { it.id == DEFAULT_LIST_ID }?.id ?: drawerItems.firstOrNull()?.id ?: DEFAULT_LIST_ID
                taskViewModel.currentSelectedListId = _selectedDrawerItemId.value
            }
        }
        drawerItems.replaceAll { it.copy(isSelectedForAction = false) }
        isDrawerSelectionModeActive = false
        closeConfirmDeleteDialog()
    }
}