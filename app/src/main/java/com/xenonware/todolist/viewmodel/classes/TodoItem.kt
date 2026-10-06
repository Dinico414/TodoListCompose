package com.xenonware.todolist.viewmodel.classes

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class TodoItem(
    @Serializable(with = StringOrIntSerializer::class)
    var id: String = "",
    var title: String = "",
    @get:Exclude var isSelectedForAction: Boolean = false,
    @PropertyName("isOffline")
    var isOffline: Boolean = false
) {
    constructor() : this(id = "", title = "", isSelectedForAction = false, isOffline = false)

    // Backwards compatibility for Firestore when id was stored as a Number
    fun setId(id: Long) {
        this.id = id.toString()
    }
}
