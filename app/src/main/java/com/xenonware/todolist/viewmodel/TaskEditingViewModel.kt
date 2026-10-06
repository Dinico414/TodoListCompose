package com.xenonware.todolist.viewmodel

import androidx.lifecycle.ViewModel
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskItem
import com.xenonware.todolist.viewmodel.classes.TaskStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskEditingViewModel : ViewModel() {

    private val _taskTitle = MutableStateFlow("")
    val taskTitle: StateFlow<String> = _taskTitle.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _priority = MutableStateFlow(Priority.LOW)
    val priority: StateFlow<Priority> = _priority.asStateFlow()

    private val _listId = MutableStateFlow(DEFAULT_LIST_ID)
    val listId: StateFlow<String> = _listId.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _dueDateMillis = MutableStateFlow<Long?>(null)
    val dueDateMillis: StateFlow<Long?> = _dueDateMillis.asStateFlow()

    private val _dueTimeHour = MutableStateFlow<Int?>(null)
    val dueTimeHour: StateFlow<Int?> = _dueTimeHour.asStateFlow()

    private val _dueTimeMinute = MutableStateFlow<Int?>(null)
    val dueTimeMinute: StateFlow<Int?> = _dueTimeMinute.asStateFlow()

    private val _steps = MutableStateFlow<List<TaskStep>>(emptyList())
    val steps: StateFlow<List<TaskStep>> = _steps.asStateFlow()

    fun setTaskTitle(title: String) { _taskTitle.value = title }
    fun setDescription(desc: String) { _description.value = desc }
    fun setPriority(priority: Priority) { _priority.value = priority }
    fun setListId(listId: String) { _listId.value = listId }
    fun setIsOffline(isOffline: Boolean) { _isOffline.value = isOffline }
    fun setDueDateMillis(millis: Long?) { _dueDateMillis.value = millis }
    fun setDueTimeHour(hour: Int?) { _dueTimeHour.value = hour }
    fun setDueTimeMinute(minute: Int?) { _dueTimeMinute.value = minute }

    fun addStep(step: TaskStep) {
        _steps.value += step
    }

    fun updateStep(index: Int, step: TaskStep) {
        val current = _steps.value.toMutableList()
        if (index in current.indices) {
            current[index] = step
            _steps.value = current
        }
    }

    fun removeStep(index: Int) {
        val current = _steps.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _steps.value = current
        }
    }

    fun setFromTask(task: TaskItem) {
        _taskTitle.value = task.task
        _description.value = task.description.orEmpty()
        _priority.value = task.priority
        _listId.value = task.listId
        _isOffline.value = task.isOffline
        _dueDateMillis.value = task.dueDateMillis
        _dueTimeHour.value = task.dueTimeHour
        _dueTimeMinute.value = task.dueTimeMinute
        _steps.value = task.steps
    }

    fun clearAllStates(defaultListId: String = DEFAULT_LIST_ID) {
        _taskTitle.value = ""
        _description.value = ""
        _priority.value = Priority.LOW
        _listId.value = defaultListId
        _isOffline.value = false
        _dueDateMillis.value = null
        _dueTimeHour.value = null
        _dueTimeMinute.value = null
        _steps.value = emptyList()
    }
}
