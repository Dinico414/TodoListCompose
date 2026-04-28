package com.xenonware.todolist.viewmodel.classes

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class TaskItem(
    val id: String = "",
    val task: String = "",
    val description: String? = null,
    val notificationCount: Int = 0,
    val priority: Priority = Priority.LOW,
    val stepCount: Int = 0,
    val attachmentCount: Int = 0,
    var isCompleted: Boolean = false,
    var listId: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeHour: Int? = null,
    val dueTimeMinute: Int? = null,
    val creationTimestamp: Long = System.currentTimeMillis(),
    var displayOrder: Int = 0,
    val steps: List<TaskStep> = emptyList(),

    @PropertyName("isOffline")
    var isOffline: Boolean = false
) {
    constructor() : this(id = "", task = "", listId = "", isOffline = false)

    @get:Exclude
    val isHighImportance: Boolean
        get() = priority == Priority.HIGH || priority == Priority.HIGHEST

    @get:Exclude
    val isHighestImportance: Boolean
        get() = priority == Priority.HIGHEST

    @get:Exclude
    var currentHeader = ""
}

@Serializable
enum class Priority {
    LOW, HIGH, HIGHEST
}