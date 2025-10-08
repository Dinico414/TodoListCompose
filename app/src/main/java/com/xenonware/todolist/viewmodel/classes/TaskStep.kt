package com.xenonware.todolist.viewmodel.classes

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TaskStep(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val displayOrder: Int = 0
)