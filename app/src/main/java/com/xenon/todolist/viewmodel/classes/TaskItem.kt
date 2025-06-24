package com.xenon.todolist.viewmodel.classes

data class TodoItem(
    val id: Int,
    val task: String,
    val description: String? = null,
    var isCompleted: Boolean = false
)