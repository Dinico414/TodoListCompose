package com.xenon.todolist.viewmodel.classes

data class TodoItem(
    val id: String,
    val title: String,
    var isSelectedForAction: Boolean = false
)