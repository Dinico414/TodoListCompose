package com.xenonware.todolist

import com.xenonware.todolist.viewmodel.DEFAULT_LIST_ID
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskItem
import com.xenonware.todolist.viewmodel.classes.TaskStep
import com.xenonware.todolist.viewmodel.classes.TodoItem
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun legacyTaskItemWithIntId_decodesSuccessfully() {
        val legacyJson = """
            [
                {
                    "id": 101,
                    "task": "Legacy Task with Int ID",
                    "description": "Old task",
                    "priority": "HIGH",
                    "isCompleted": true,
                    "listId": "custom_list_1",
                    "isOffline": false
                },
                {
                    "id": "202",
                    "task": "New Task with String ID",
                    "priority": "LOW",
                    "isCompleted": false,
                    "listId": ""
                }
            ]
        """.trimIndent()

        val decoded = json.decodeFromString<List<TaskItem>>(legacyJson)
        assertEquals(2, decoded.size)

        // First item (integer ID converted to string)
        assertEquals("101", decoded[0].id)
        assertEquals("Legacy Task with Int ID", decoded[0].task)
        assertEquals(Priority.HIGH, decoded[0].priority)
        assertTrue(decoded[0].isCompleted)
        assertEquals("custom_list_1", decoded[0].listId)

        // Second item (string ID)
        assertEquals("202", decoded[1].id)
        assertEquals("New Task with String ID", decoded[1].task)
        assertFalse(decoded[1].isCompleted)

        // Empty listId fallback
        val normalized = decoded.map {
            if (it.listId.isBlank()) it.copy(listId = DEFAULT_LIST_ID) else it
        }
        assertEquals(DEFAULT_LIST_ID, normalized[1].listId)
    }

    @Test
    fun legacyTaskStep_decodesSuccessfully() {
        val legacyStepsJson = """
            [
                {
                    "id": 1,
                    "text": "Old Step with numeric id",
                    "isCompleted": false,
                    "displayOrder": 0
                },
                {
                    "id": "uuid-string-step-2",
                    "text": "New Step with string id",
                    "isCompleted": true,
                    "displayOrder": 1
                }
            ]
        """.trimIndent()

        val decoded = json.decodeFromString<List<TaskStep>>(legacyStepsJson)
        assertEquals(2, decoded.size)
        assertEquals("1", decoded[0].id)
        assertFalse(decoded[0].isCompleted)
        assertEquals("uuid-string-step-2", decoded[1].id)
        assertTrue(decoded[1].isCompleted)
    }

    @Test
    fun legacyTodoItemWithIntId_decodesSuccessfully() {
        val legacyTodoJson = """
            [
                {
                    "id": 12,
                    "title": "Legacy List with numeric id",
                    "isOffline": false
                },
                {
                    "id": "default_my_tasks_list_id",
                    "title": "My Tasks",
                    "isOffline": false
                }
            ]
        """.trimIndent()

        val decoded = json.decodeFromString<List<TodoItem>>(legacyTodoJson)
        assertEquals(2, decoded.size)
        assertEquals("12", decoded[0].id)
        assertEquals("Legacy List with numeric id", decoded[0].title)
        assertEquals("default_my_tasks_list_id", decoded[1].id)
    }

    @Test
    fun firestoreSetterBackwardsCompatibility() {
        val task = TaskItem()
        // Simulate Firestore calling setId(Long) for legacy numeric id
        task.setId(999L)
        assertEquals("999", task.id)

        // Simulate Firestore calling setIsCompleted(Boolean)
        task.setIsCompleted(true)
        assertTrue(task.isCompleted)

        // Simulate Firestore calling setPriority(Long)
        task.setPriority(1L)
        assertEquals(Priority.HIGH, task.priority)

        val step = TaskStep()
        step.setId(55L)
        assertEquals("55", step.id)
        step.setIsCompleted(true)
        assertTrue(step.isCompleted)
    }
}
