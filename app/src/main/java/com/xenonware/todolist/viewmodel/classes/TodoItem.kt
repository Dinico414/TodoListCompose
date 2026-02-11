package com.xenonware.todolist.viewmodel.classes

import com.google.firebase.firestore.Exclude
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class TodoItem(
    val id: String = "",
    var title: String = "",
    @get:Exclude var isSelectedForAction: Boolean = false
) {
    constructor() : this(id = "", title = "", isSelectedForAction = false)
}