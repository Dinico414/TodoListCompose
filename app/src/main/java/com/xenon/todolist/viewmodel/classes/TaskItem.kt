package com.xenon.todolist.viewmodel.classes

data class TodoItem(
    val id: Int,
    val task: String,
    val description: String? = null,
    val notificationCount: Int = 0,
    val isHighImportance: Boolean = false,
    val isHighestImportance: Boolean = false,
    val stepCount: Int = 0,
    val attachmentCount: Int = 0,
    var isCompleted: Boolean = false
)