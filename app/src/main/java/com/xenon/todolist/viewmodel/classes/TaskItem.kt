package com.xenon.todolist.viewmodel.classes

import com.xenon.todolist.viewmodel.Priority

data class TaskItem(
    val id: Int,
    val task: String,
    val description: String? = null,
    val notificationCount: Int = 0,
    val priority: Priority = Priority.LOW,
    val stepCount: Int = 0,
    val attachmentCount: Int = 0,
    var isCompleted: Boolean = false
) {
    val isHighImportance: Boolean
        get() = priority == Priority.HIGH || priority == Priority.HIGHEST

    val isHighestImportance: Boolean
        get() = priority == Priority.HIGHEST
}