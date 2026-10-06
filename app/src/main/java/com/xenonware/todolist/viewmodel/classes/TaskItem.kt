package com.xenonware.todolist.viewmodel.classes

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

object StringOrIntSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            return if (element is JsonPrimitive) {
                element.content
            } else {
                element.toString()
            }
        }
        return decoder.decodeString()
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

@Suppress("unused")
@Serializable
data class TaskItem(
    @Serializable(with = StringOrIntSerializer::class)
    var id: String = "",
    val task: String = "",
    val description: String? = null,
    val notificationCount: Int = 0,
    var priority: Priority = Priority.LOW,
    val stepCount: Int = 0,
    val attachmentCount: Int = 0,
    var isCompleted: Boolean = false,
    var listId: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeHour: Int? = null,
    val dueTimeMinute: Int? = null,
    val creationTimestamp: Long = System.currentTimeMillis(),
    var displayOrder: Int = 0,
    val steps: List<TaskStep> = emptyList(),

    @PropertyName("isOffline")
    var isOffline: Boolean = false
) {
    constructor() : this(id = "", task = "", listId = "", isOffline = false)

    // Backwards compatibility for Firestore when id was stored as a Number
    fun setId(id: Long) {
        this.id = id.toString()
    }

    // Backwards compatibility for Firestore when isCompleted was stored as "completed"
    fun setIsCompleted(isCompleted: Boolean) {
        this.isCompleted = isCompleted
    }

    // Backwards compatibility for Firestore when priority was stored as a Number
    fun setPriority(priorityNumber: Long) {
        this.priority = Priority.entries.getOrElse(priorityNumber.toInt()) { Priority.LOW }
    }

    @get:Exclude
    val isHighImportance: Boolean
        get() = priority == Priority.HIGH || priority == Priority.HIGHEST

    @get:Exclude
    val isHighestImportance: Boolean
        get() = priority == Priority.HIGHEST

    @get:Exclude
    var currentHeader = ""
}

@Serializable
enum class Priority {
    LOW, HIGH, HIGHEST
}
