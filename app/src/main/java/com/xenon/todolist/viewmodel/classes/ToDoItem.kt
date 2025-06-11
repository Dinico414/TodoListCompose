package com.xenon.todolist.viewmodel.classes

data class TodoItem(
    val id: Int,
    val task: String,
    var isCompleted: Boolean = false
)