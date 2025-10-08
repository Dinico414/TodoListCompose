package com.xenonware.todolist.ui.layouts

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.xenonware.todolist.ui.layouts.todo.CompactTodo
import com.xenonware.todolist.ui.layouts.todo.CoverTodo
import com.xenonware.todolist.viewmodel.LayoutType
import com.xenonware.todolist.viewmodel.TaskViewModel

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
                CoverTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    appSize = appSize,
                )
            } else {
                CoverTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize,
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
                    appSize = appSize,
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize,
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
                    appSize = appSize,
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize,
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
                    appSize = appSize,
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize,
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
                    appSize = appSize,
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    appSize = appSize,
                )
            }
        }
    }
}
