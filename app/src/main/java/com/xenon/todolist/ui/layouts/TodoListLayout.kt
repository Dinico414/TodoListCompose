package com.xenon.todolist.ui.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xenon.todolist.ui.layouts.todo.CompactTodo
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TodoViewModel


@Composable
fun TodoListLayout(
    viewModel: TodoViewModel,
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
                    modifier = modifier,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    modifier = modifier,
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
                    modifier = modifier,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    modifier = modifier,
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
                    modifier = modifier,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    modifier = modifier,
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
                    modifier = modifier,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    modifier = modifier,
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
                    modifier = modifier,
                    isLandscape = true
                )
            } else {
                CompactTodo(
                    onOpenSettings = onOpenSettings,
                    viewModel = viewModel,
                    layoutType = layoutType,
                    modifier = modifier,
                    isLandscape = false
                )
            }
        }
    }
}
