package com.xenon.todolist.ui.layouts

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.xenon.todolist.ui.layouts.todo.CompactTodo
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun TodoListLayout(
    viewModel: TaskViewModel,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    layoutType: LayoutType,
    onOpenSettings: () -> Unit,
    appSize: IntSize,
) {

    when (layoutType) {
        LayoutType.COVER -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    appSize = appSize, // Pass appSize
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize, // Pass appSize
                )
            }
        }

        LayoutType.SMALL -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    appSize = appSize, // Pass appSize
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize, // Pass appSize
                )
            }
        }

        LayoutType.COMPACT -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    appSize = appSize, // Pass appSize
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize, // Pass appSize
                )
            }
        }

        LayoutType.MEDIUM -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    appSize = appSize, // Pass appSize
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize, // Pass appSize
                )
            }
        }

        LayoutType.EXPANDED -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    appSize = appSize, // Pass appSize
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize, // Pass appSize
                )
            }
        }
    }
}
