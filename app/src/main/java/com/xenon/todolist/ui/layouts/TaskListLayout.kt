package com.xenon.todolist.ui.layouts

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    widthSizeClass: WindowWidthSizeClass,
) {

    when (layoutType) {
        LayoutType.COVER -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true,
                    widthSizeClass = widthSizeClass
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    widthSizeClass = widthSizeClass
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
                    widthSizeClass = widthSizeClass
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    widthSizeClass = widthSizeClass
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
                    widthSizeClass = widthSizeClass
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    widthSizeClass = widthSizeClass
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
                    widthSizeClass = widthSizeClass
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    widthSizeClass = widthSizeClass
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
                    widthSizeClass = widthSizeClass
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    taskViewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false,
                    widthSizeClass = widthSizeClass
                )
            }
        }
    }
}
