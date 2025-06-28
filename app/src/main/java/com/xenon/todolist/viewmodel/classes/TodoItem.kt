package com.xenon.todolist.viewmodel.classes

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    var title: String,
    var isSelectedForAction: Boolean = false
)