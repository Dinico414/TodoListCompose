@file:Suppress("unused")

package com.xenonware.todolist.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.xenonware.todolist.data.SharedPreferenceManager
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskItem
import com.xenonware.todolist.viewmodel.classes.TaskStep
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class SortOption { FREE_SORTING, CREATION_DATE, DUE_DATE, COMPLETENESS, NAME, IMPORTANCE }
enum class SortOrder { ASCENDING, DESCENDING }
enum class FilterState { INCLUDED, EXCLUDED }

enum class FilterableAttribute {
    HAS_DESCRIPTION,
    IS_LOW_PRIORITY,
    IS_HIGH_PRIORITY,
    IS_HIGHEST_PRIORITY,
    HAS_DUE_DATE,
    HAS_DUE_TIME;

    fun toDisplayString(): String = when (this) {
        HAS_DESCRIPTION -> "Has Description"
        IS_LOW_PRIORITY -> "Low Importance"
        IS_HIGH_PRIORITY -> "High Importance"
        IS_HIGHEST_PRIORITY -> "Highest Importance"
        HAS_DUE_DATE -> "Has Due Date"
        HAS_DUE_TIME -> "Has Due Time"
    }
}

sealed class SnackbarEvent {
    data class ShowUndoDeleteSnackbar(val taskItem: TaskItem) : SnackbarEvent()
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferenceManager(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _allTaskItems = mutableStateListOf<TaskItem>()
    private val _displayedTaskItems = mutableStateListOf<Any>()
    val taskItems: List<Any> get() = _displayedTaskItems

    private var currentTaskId = 1

    // Firestore sync tracking
    private val syncingTaskIds = mutableStateSetOf<Int>()
    private val offlineTaskIds = mutableStateSetOf<Int>()

    private val _showTaskSheet = MutableStateFlow(false)
    val showTaskSheet: StateFlow<Boolean> = _showTaskSheet.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskItem?>(null)
    val editingTask: StateFlow<TaskItem?> = _editingTask.asStateFlow()

    fun showTaskSheetForNewTask() {
        _editingTask.value = null
        _showTaskSheet.value = true
    }

    fun showTaskSheetForEdit(task: TaskItem) {
        _editingTask.value = task
        _showTaskSheet.value = true
    }

    fun hideTaskSheet() {
        _showTaskSheet.value = false
        _editingTask.value = null
    }

    private var recentlyDeletedItem: TaskItem? = null
    private var recentlyDeletedItemOriginalIndex: Int = -1

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent: SharedFlow<SnackbarEvent> = _snackbarEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var itemToDeleteOnConfirm: TaskItem? = null

    var currentSelectedListId: String? = DEFAULT_LIST_ID
        set(value) {
            if (field != value) {
                field = value
                applySortingAndFiltering()
            }
        }

    var currentSortOption: SortOption by mutableStateOf(SortOption.FREE_SORTING)
        private set
    var currentSortOrder: SortOrder by mutableStateOf(SortOrder.ASCENDING)
        private set

    var filterStates = mutableStateMapOf<FilterableAttribute, FilterState>()
        private set

    init {
        loadAllTasks()
        applySortingAndFiltering()

        auth.currentUser?.uid?.let { uid ->
            startRealtimeListenerForFutureChanges(uid)
        }
    }


    fun onSignedIn() {
        val uid = auth.currentUser?.uid ?: return
        startRealtimeListenerForFutureChanges(uid)
        uploadPendingOfflineTasks(uid)
    }

    private fun startRealtimeListenerForFutureChanges(userId: String) {
        firestore.collection("tasks").document(userId).collection("user_tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->
                    val task = change.document.toObject(TaskItem::class.java)

                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            if (!offlineTaskIds.contains(task.id) && _allTaskItems.none { it.id == task.id }) {
                                _allTaskItems.add(0, task.copy(isOffline = false))
                                saveAllTasks()
                                applySortingAndFiltering()
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = _allTaskItems.indexOfFirst { it.id == task.id }
                            if (index != -1 && !offlineTaskIds.contains(task.id)) {
                                _allTaskItems[index] = task.copy(isOffline = false)
                                saveAllTasks()
                                applySortingAndFiltering()
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            if (!offlineTaskIds.contains(task.id)) {
                                _allTaskItems.removeAll { it.id == task.id }
                                saveAllTasks()
                                applySortingAndFiltering()
                            }
                        }
                    }
                }
            }
    }

    private fun uploadPendingOfflineTasks(userId: String) {
        viewModelScope.launch {
            _allTaskItems.filter { it.isOffline }.forEach { task ->
                if (task.id in syncingTaskIds) return@forEach
                syncingTaskIds.add(task.id)
                try {
                    firestore.collection("tasks").document(userId).collection("user_tasks")
                        .document(task.id.toString())
                        .set(task.copy(isOffline = false))
                        .await()

                    offlineTaskIds.remove(task.id)
                    syncingTaskIds.remove(task.id)

                    val idx = _allTaskItems.indexOfFirst { it.id == task.id }
                    if (idx != -1) _allTaskItems[idx] = _allTaskItems[idx].copy(isOffline = false)

                    saveAllTasks()
                    applySortingAndFiltering()
                } catch (_: Exception) {
                    syncingTaskIds.remove(task.id)
                }
            }
        }
    }

    // ──────────────────────── CRUD WITH SYNC ────────────────────────

    fun saveTask(
        taskText: String,
        description: String?,
        priority: Priority,
        dueDateMillis: Long?,
        dueTimeHour: Int?,
        dueTimeMinute: Int?,
        steps: List<TaskStep>
    ) {
        val editing = _editingTask.value

        if (editing != null) {
            // update
            val updated = editing.copy(
                task = taskText.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDateMillis = dueDateMillis,
                dueTimeHour = dueTimeHour,
                dueTimeMinute = dueTimeMinute,
                steps = steps
            )
            updateItem(updated)
        } else {
            // create new
            addItem(
                task = taskText.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDateMillis = dueDateMillis,
                dueTimeHour = dueTimeHour,
                dueTimeMinute = dueTimeMinute,
                steps = steps
            )
        }

        hideTaskSheet()
    }

    fun addItem(
        task: String,
        description: String? = null,
        priority: Priority = Priority.LOW,
        dueDateMillis: Long? = null,
        dueTimeHour: Int? = null,
        dueTimeMinute: Int? = null,
        steps: List<TaskStep> = emptyList(),
        forceLocal: Boolean = false
    ) {
        if (task.isBlank() || currentSelectedListId == null) return

        val newId = currentTaskId++
        val newTask = TaskItem(
            id = newId,
            task = task.trim(),
            description = description?.trim()?.takeIf { it.isNotBlank() },
            priority = priority,
            isCompleted = false,
            listId = currentSelectedListId!!,
            dueDateMillis = dueDateMillis,
            dueTimeHour = dueTimeHour,
            dueTimeMinute = dueTimeMinute,
            creationTimestamp = System.currentTimeMillis(),
            displayOrder = determineNextDisplayOrder(currentSelectedListId!!),
            steps = steps,
            isOffline = forceLocal
        )

        _allTaskItems.add(newTask)
        saveAllTasks()
        applySortingAndFiltering()

        if (forceLocal) {
            offlineTaskIds.add(newId)
        } else if (auth.currentUser != null) {
            syncingTaskIds.add(newId)
            val uid = auth.currentUser!!.uid
            firestore.collection("tasks").document(uid).collection("user_tasks")
                .document(newId.toString())
                .set(newTask)
                .addOnSuccessListener {
                    syncingTaskIds.remove(newId)
                    applySortingAndFiltering()
                }
        }
    }

    fun updateItem(updatedItem: TaskItem, forceLocal: Boolean = false) {
        val index = _allTaskItems.indexOfFirst { it.id == updatedItem.id }
        if (index == -1) return

        val old = _allTaskItems[index]
        val wasOffline = old.isOffline
        val nowOffline = forceLocal || updatedItem.isOffline

        val finalTask = updatedItem.copy(isOffline = nowOffline)
        _allTaskItems[index] = finalTask

        if (nowOffline) offlineTaskIds.add(finalTask.id) else offlineTaskIds.remove(finalTask.id)

        saveAllTasks()
        applySortingAndFiltering()

        if (!nowOffline && auth.currentUser != null) {
            syncingTaskIds.add(finalTask.id)
            viewModelScope.launch {
                try {
                    firestore.collection("tasks")
                        .document(auth.currentUser!!.uid)
                        .collection("user_tasks")
                        .document(finalTask.id.toString())
                        .set(finalTask)
                        .await()
                    syncingTaskIds.remove(finalTask.id)
                    applySortingAndFiltering()
                } catch (_: Exception) {
                    syncingTaskIds.remove(finalTask.id)
                }
            }
        } else if (!wasOffline && nowOffline && auth.currentUser != null) {
            viewModelScope.launch {
                try {
                    firestore.collection("tasks")
                        .document(auth.currentUser!!.uid)
                        .collection("user_tasks")
                        .document(finalTask.id.toString())
                        .delete()
                        .await()
                } catch (_: Exception) {}
            }
        }
    }

    // ──────────────────────── YOUR ORIGINAL FEATURES (100% preserved) ────────────────────────

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            applySortingAndFiltering()
        }
    }

    private fun loadAllTasks() {
        currentSortOption = prefsManager.sortOption
        currentSortOrder = prefsManager.sortOrder
        val loadedTasks = prefsManager.taskItems
        _allTaskItems.clear()
        _allTaskItems.addAll(loadedTasks)
        currentTaskId = if (loadedTasks.isNotEmpty()) {
            (loadedTasks.maxOfOrNull { it.id } ?: 0) + 1
        } else 1
    }

    fun saveAllTasks() {
        prefsManager.taskItems = _allTaskItems.toList()
    }

    fun swapDisplayOrder(from: Int, to: Int) {
        val item1 = taskItems[from] as? TaskItem ?: return
        val item2 = taskItems[to] as? TaskItem ?: return

        val tmp = item1.displayOrder
        item1.displayOrder = item2.displayOrder
        item2.displayOrder = tmp

        _displayedTaskItems.add(to, _displayedTaskItems.removeAt(from))
    }

    private fun determineNextDisplayOrder(forListId: String): Int {
        return _allTaskItems
            .filter { it.listId == forListId }
            .maxOfOrNull { it.displayOrder }?.plus(1) ?: 0
    }

    fun prepareRemoveItem(itemId: Int) {
        val itemIndex = _allTaskItems.indexOfFirst { it.id == itemId }
        if (itemIndex != -1) {
            val item = _allTaskItems[itemIndex]
            recentlyDeletedItem = item
            recentlyDeletedItemOriginalIndex = itemIndex
            itemToDeleteOnConfirm = item // ← remember for later sync

            _allTaskItems.removeAt(itemIndex)
            applySortingAndFiltering(preserveRecentlyDeleted = true)

            viewModelScope.launch {
                _snackbarEvent.emit(SnackbarEvent.ShowUndoDeleteSnackbar(item))
            }
        }
    }

    fun undoRemoveItem() {
        recentlyDeletedItem?.let { itemToRestore ->
            if (recentlyDeletedItemOriginalIndex != -1 && recentlyDeletedItemOriginalIndex <= _allTaskItems.size) {
                _allTaskItems.add(recentlyDeletedItemOriginalIndex, itemToRestore)
            } else {
                _allTaskItems.add(itemToRestore)
            }

            itemToDeleteOnConfirm = null

            recentlyDeletedItem = null
            recentlyDeletedItemOriginalIndex = -1
            applySortingAndFiltering()

            saveAllTasks()
        }
    }

    fun confirmRemoveItem() {
        itemToDeleteOnConfirm?.let { item ->
            if (auth.currentUser != null && !item.isOffline) {
                viewModelScope.launch {
                    try {
                        firestore.collection("tasks")
                            .document(auth.currentUser!!.uid)
                            .collection("user_tasks")
                            .document(item.id.toString())
                            .delete()
                            .await()
                    } catch (_: Exception) { /* best effort */ }
                }
            }
        }

        saveAllTasks()
        recentlyDeletedItem = null
        recentlyDeletedItemOriginalIndex = -1
        itemToDeleteOnConfirm = null
    }

    fun toggleCompleted(itemId: Int) {
        val indexInAll = _allTaskItems.indexOfFirst { it.id == itemId }
        if (indexInAll != -1) {
            val oldItem = _allTaskItems[indexInAll]
            val updatedItem = oldItem.copy(isCompleted = !oldItem.isCompleted)
            _allTaskItems[indexInAll] = updatedItem

            saveAllTasks()
            applySortingAndFiltering()

            if (auth.currentUser != null && !updatedItem.isOffline) {
                updateItem(updatedItem)
            }
        }
    }

    fun clearTasksForList(listIdToClear: String) {
        if (recentlyDeletedItem?.listId == listIdToClear) {
            recentlyDeletedItem = null
            recentlyDeletedItemOriginalIndex = -1
        }
        val tasksWereRemoved = _allTaskItems.removeAll { it.listId == listIdToClear }
        if (tasksWereRemoved) {
            saveAllTasks()
            applySortingAndFiltering()
        }
    }

    fun setSortCriteria(option: SortOption, order: SortOrder) {
        if (currentSortOption != option || currentSortOrder != order) {
            currentSortOption = option
            currentSortOrder = order
            prefsManager.sortOption = option
            prefsManager.sortOrder = order
            applySortingAndFiltering()
        }
    }

    fun updateMultipleFilterStates(newStates: Map<FilterableAttribute, FilterState>) {
        var changed = false
        val attributesToRemove = filterStates.keys.filterNot { it in newStates.keys }
        attributesToRemove.forEach { attribute ->
            if (filterStates.remove(attribute) != null) changed = true
        }
        newStates.forEach { (attribute, newState) ->
            if (filterStates[attribute] != newState) {
                filterStates[attribute] = newState
                changed = true
            }
        }
        if (changed) applySortingAndFiltering()
    }

    fun resetAllFilters() {
        if (filterStates.isNotEmpty()) {
            filterStates.clear()
            applySortingAndFiltering()
        }
    }

    // ──────────────────────── STEPS ────────────────────────

    fun addStepToTask(taskId: Int, stepText: String) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1 && stepText.isNotBlank()) {
            val task = _allTaskItems[taskIndex]
            val newStep = TaskStep(
                id = UUID.randomUUID().toString(),
                text = stepText.trim(),
                isCompleted = false,
                displayOrder = task.steps.size
            )
            val updatedTask = task.copy(steps = task.steps + newStep)
            _allTaskItems[taskIndex] = updatedTask

            saveAllTasks()
            applySortingAndFiltering()

            if (auth.currentUser != null && !updatedTask.isOffline) {
                updateItem(updatedTask)
            }
        }
    }

    fun toggleStepCompletion(taskId: Int, stepId: String) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = _allTaskItems[taskIndex]
            val stepIndex = task.steps.indexOfFirst { it.id == stepId }
            if (stepIndex != -1) {
                val step = task.steps[stepIndex]
                val updatedStep = step.copy(isCompleted = !step.isCompleted)
                val updatedSteps = task.steps.toMutableList().apply {
                    this[stepIndex] = updatedStep
                }
                val updatedTask = task.copy(steps = updatedSteps)

                _allTaskItems[taskIndex] = updatedTask
                saveAllTasks()
                applySortingAndFiltering()

                if (auth.currentUser != null && !updatedTask.isOffline) {
                    updateItem(updatedTask)
                }
            }
        }
    }
    fun removeStepFromTask(taskId: Int, stepId: String) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = _allTaskItems[taskIndex]
            val updatedSteps = task.steps.filterNot { it.id == stepId }
            if (updatedSteps.size != task.steps.size) {
                _allTaskItems[taskIndex] = task.copy(steps = updatedSteps)
                saveAllTasks()
                applySortingAndFiltering()
            }
        }
    }

    fun updateStepInTask(taskId: Int, updatedStep: TaskStep) {
        val taskIndex = _allTaskItems.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = _allTaskItems[taskIndex]
            val stepIndex = task.steps.indexOfFirst { it.id == updatedStep.id }
            if (stepIndex != -1) {
                val updatedSteps = task.steps.toMutableList()
                updatedSteps[stepIndex] = updatedStep
                _allTaskItems[taskIndex] = task.copy(steps = updatedSteps)
                saveAllTasks()
                applySortingAndFiltering()
            }
        }
    }

    // ──────────────────────── SORTING & FILTERING (your exact original code) ────────────────────────

    private fun applySortingAndFiltering(preserveRecentlyDeleted: Boolean = false) {
        val currentRecentlyDeleted = if (preserveRecentlyDeleted) recentlyDeletedItem else null
        val tempAllTaskItems = _allTaskItems.toMutableList()
        when {
            currentRecentlyDeleted != null && !tempAllTaskItems.contains(currentRecentlyDeleted) -> {
            }
        }

        _displayedTaskItems.clear()
        var tasksToProcess = if (currentSelectedListId != null) {
            _allTaskItems.filter { it.listId == currentSelectedListId }
        } else emptyList()

        val currentQuery = searchQuery.value
        if (currentQuery.isNotBlank()) {
            tasksToProcess = tasksToProcess.filter { task ->
                task.task.contains(currentQuery, ignoreCase = true) ||
                        (task.description?.contains(currentQuery, ignoreCase = true) == true)
            }
        }

        if (filterStates.isNotEmpty()) {
            tasksToProcess = tasksToProcess.filter { task ->
                val includedFilters = filterStates.filterValues { it == FilterState.INCLUDED }.keys
                val matchesIncluded = if (includedFilters.isNotEmpty()) {
                    includedFilters.any { attribute -> task.matchesAttribute(attribute) }
                } else true

                val excludedFilters = filterStates.filterValues { it == FilterState.EXCLUDED }.keys
                val matchesExcluded = excludedFilters.none { attribute -> task.matchesAttribute(attribute) }

                matchesIncluded && matchesExcluded
            }
        }

        val sortedTasks = sortTasks(tasksToProcess, currentSortOption, currentSortOrder)

        if (currentSortOption != SortOption.FREE_SORTING && sortedTasks.isNotEmpty()) {
            val groupedItems = mutableListOf<Any>()
            var lastHeader: String? = null
            for (task in sortedTasks) {
                task.currentHeader = getHeaderForTask(task, currentSortOption, currentSortOrder)
                if (task.currentHeader != lastHeader) {
                    groupedItems.add(task.currentHeader)
                    lastHeader = task.currentHeader
                }
                groupedItems.add(task)
            }
            _displayedTaskItems.addAll(groupedItems)
        } else {
            sortedTasks.forEach { it.currentHeader = "" }
            _displayedTaskItems.addAll(sortedTasks)
        }
        // Insert back for undo preview (your original logic had empty block – kept as-is)
    }

    private fun getHeaderForTask(task: TaskItem, sortOption: SortOption, sortOrder: SortOrder): String {
        return when (sortOption) {
            SortOption.COMPLETENESS -> if (task.isCompleted) "Completed" else "Not Completed"
            SortOption.IMPORTANCE -> "Importance: ${task.priority.name.lowercase().replaceFirstChar { it.titlecase() }}"
            SortOption.DUE_DATE -> {
                if (task.dueDateMillis == null) "No Due Date"
                else SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDateMillis))
            }
            SortOption.CREATION_DATE -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.creationTimestamp))
            SortOption.NAME -> task.task.firstOrNull()?.uppercaseChar()?.toString() ?: "Unknown"
            SortOption.FREE_SORTING -> ""
        }
    }

    private fun TaskItem.matchesAttribute(attribute: FilterableAttribute): Boolean {
        return when (attribute) {
            FilterableAttribute.HAS_DESCRIPTION -> this.description?.isNotBlank() == true
            FilterableAttribute.IS_LOW_PRIORITY -> this.priority == Priority.LOW
            FilterableAttribute.IS_HIGH_PRIORITY -> this.priority == Priority.HIGH
            FilterableAttribute.IS_HIGHEST_PRIORITY -> this.priority == Priority.HIGHEST
            FilterableAttribute.HAS_DUE_DATE -> this.dueDateMillis != null
            FilterableAttribute.HAS_DUE_TIME -> this.dueTimeHour != null && this.dueTimeMinute != null
        }
    }

    private fun sortTasks(tasks: List<TaskItem>, option: SortOption, order: SortOrder): List<TaskItem> {
        val comparator: Comparator<TaskItem> = when (option) {
            SortOption.FREE_SORTING -> compareBy { it.displayOrder }
            SortOption.CREATION_DATE -> compareBy<TaskItem> { it.creationTimestamp }.thenBy { it.displayOrder }
            SortOption.DUE_DATE -> compareByDescending<TaskItem> { it.dueDateMillis == null }
                .thenBy { it.dueDateMillis }.thenBy { it.displayOrder }
            SortOption.COMPLETENESS -> compareBy<TaskItem> { it.isCompleted }.thenBy { it.displayOrder }
            SortOption.NAME -> compareBy<TaskItem, String>(String.CASE_INSENSITIVE_ORDER) { it.task }.thenBy { it.displayOrder }
            SortOption.IMPORTANCE -> compareByDescending<TaskItem> { it.priority.ordinal }.thenBy { it.displayOrder }
        }
        return if (order == SortOrder.ASCENDING) tasks.sortedWith(comparator) else tasks.sortedWith(comparator.reversed())
    }

    companion object {
        const val DEFAULT_LIST_ID = "default_list"
    }
}