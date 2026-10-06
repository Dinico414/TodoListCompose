// File: com/xenonware/todolist/viewmodel/classes/TaskStep.kt

package com.xenonware.todolist.viewmodel.classes

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TaskStep(
    @Serializable(with = StringOrIntSerializer::class)
    var id: String = UUID.randomUUID().toString(),
    val text: String = "",
    @get:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val displayOrder: Int = 0
) {
    // Required for Firestore to read old data
    constructor() : this(
        id = UUID.randomUUID().toString(),
        text = "",
        isCompleted = false,
        displayOrder = 0
    )

    // Backwards compatibility for Firestore when id was stored as a Number
    fun setId(id: Long) {
        this.id = id.toString()
    }

    // Backwards compatibility for Firestore when field was "completed"
    fun setIsCompleted(isCompleted: Boolean) {
        this.isCompleted = isCompleted
    }
}
