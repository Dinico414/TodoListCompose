// File: com/xenonware/todolist/viewmodel/classes/TaskStep.kt

package com.xenonware.todolist.viewmodel.classes

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TaskStep(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isCompleted: Boolean = false,
    val displayOrder: Int = 0
) {
    // Required for Firestore to read old data that used "completed" field
    constructor() : this(
        id = UUID.randomUUID().toString(),
        text = "",
        isCompleted = false,
        displayOrder = 0
    )

    // This ignores the old "completed" field that might still exist in Firestore
    @Suppress("unused")
    private val completed: Boolean? = null
}