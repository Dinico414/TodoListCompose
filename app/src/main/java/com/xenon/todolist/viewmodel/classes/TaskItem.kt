package com.xenon.todolist.viewmodel.classes

import kotlinx.serialization.Serializable

@Serializable
data class TaskItem(
    val id: Int,
    val task: String,
    val description: String? = null,
    val notificationCount: Int = 0,
    val priority: Priority = Priority.LOW,
    val stepCount: Int = 0,
    val attachmentCount: Int = 0,
    var isCompleted: Boolean = false,
    var listId: String
) {
    val isHighImportance: Boolean
        get() = priority == Priority.HIGH || priority == Priority.HIGHEST
    
    val isHighestImportance: Boolean
        get() = priority == Priority.HIGHEST
}

@Serializable
enum class Priority {
    LOW, HIGH, HIGHEST
}