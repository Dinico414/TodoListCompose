package com.xenonware.todolist.ui.layouts

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize
import com.xenonware.todolist.ui.layouts.todo.CompactTodo
import com.xenonware.todolist.ui.layouts.todo.CoverTodo
import com.xenonware.todolist.viewmodel.LayoutType
import com.xenonware.todolist.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainLayout(
    viewModel: TaskViewModel,
    isLandscape: Boolean,
    layoutType: LayoutType,
    onOpenSettings: () -> Unit,
    appSize: IntSize,
) {

    when (layoutType) {
        LayoutType.COVER -> {
            if (isLandscape) {
                CoverTodo(
                    viewModel = viewModel,
                    onOpenSettings = onOpenSettings,
                )
            } else {
                CoverTodo(
                    viewModel = viewModel,
                    onOpenSettings = onOpenSettings,
                )
            }
        }

        LayoutType.SMALL -> {
            if (isLandscape) {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = true,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            } else {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = false,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            }
        }

        LayoutType.COMPACT -> {
            if (isLandscape) {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = true,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            } else {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = false,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            }
        }

        LayoutType.MEDIUM -> {
            if (isLandscape) {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = true,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            } else {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = false,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            }
        }

        LayoutType.EXPANDED -> {
            if (isLandscape) {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = true,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            } else {
                CompactTodo(
                    viewModel = viewModel,
                    isLandscape = false,
                    layoutType = layoutType,
                    onOpenSettings = onOpenSettings,
                    appSize = appSize
                )
            }
        }
    }
}
