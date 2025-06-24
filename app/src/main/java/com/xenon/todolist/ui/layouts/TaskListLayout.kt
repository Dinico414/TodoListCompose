package com.xenon.todolist.ui.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xenon.todolist.ui.layouts.todo.CompactTodo
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TaskViewModel


@Composable
fun TodoListLayout(
    viewModel: TaskViewModel,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    layoutType: LayoutType,
    onOpenSettings: () -> Unit,
) {
    when (layoutType) {
        LayoutType.COVER -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false
                )
            }
        }

        LayoutType.SMALL -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false
                )
            }
        }

        LayoutType.COMPACT -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false
                )
            }
        }

        LayoutType.MEDIUM -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false
                )
            }
        }

        LayoutType.EXPANDED -> {
            if (isLandscape) {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    isLandscape = false
                )
            }
        }
    }
}
