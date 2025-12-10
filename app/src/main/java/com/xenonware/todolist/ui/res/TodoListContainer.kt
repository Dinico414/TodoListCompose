// TodoListContent.kt — FINAL PERFECT VERSION (bottomContent used, button at top, no crash)
package com.xenonware.todolist.ui.res

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.mylibrary.values.ExtraLargePadding
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.MediumPadding
import com.xenon.mylibrary.values.NoPadding
import com.xenonware.todolist.R
import com.xenonware.todolist.ui.theme.extendedMaterialColorScheme
import com.xenonware.todolist.viewmodel.DevSettingsViewModel
import com.xenonware.todolist.viewmodel.TodoViewModel
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TodoListContent(
    viewModel: TodoViewModel,
    onDrawerItemClicked: (itemId: String) -> Unit,
    devSettingsViewModel: DevSettingsViewModel = viewModel()
) {
    val drawerItems = viewModel.drawerItems
    val currentSelectedItemId = viewModel.selectedDrawerItemId.value
    val isSelectionModeActive = viewModel.isDrawerSelectionModeActive
    val showDummyProfile by devSettingsViewModel.showDummyProfileState.collectAsState()
    val isDeveloperMode by devSettingsViewModel.devModeToggleState.collectAsState()
    XenonDrawerNoGoogle(
        title = stringResource(R.string.todo_sheet_title),
        backgroundColor = colorScheme.surfaceContainerHigh,
        showProfileSection = isDeveloperMode && showDummyProfile, // your logic here
        profileIconRes = R.mipmap.default_icon,
        profileContentDescriptionRes = R.string.open_navigation_menu,
        hasBottomContent = true,
        bottomContent = {
            ActionButtonWithDivider(
                isSelectionModeActive = isSelectionModeActive,
                onAddClick = { viewModel.openAddListDialog() },
                onDeleteClick = { viewModel.openConfirmDeleteDialog() })
        },
        contentManagesScrolling = true
    ) { _ -> // we ignore scrollState — we use LazyColumn instead

        val listState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
            val item = drawerItems.removeAt(from.index)
            drawerItems.add(to.index, item)
        }

        LazyColumn(
            state = listState, contentPadding = PaddingValues(
                start = ExtraLargePadding,
                end = ExtraLargePadding,
                top = MediumPadding,
                bottom = ExtraLargePadding
            ), verticalArrangement = Arrangement.spacedBy(MediumPadding)
        ) {
            itemsIndexed(
                items = drawerItems, key = { _, item -> item.id }) { index, item ->
                ReorderableItem(
                    state = reorderableState, key = item.id, enabled = index != 0
                ) { isDragging ->
                    TodoListCell(
                        item = item,
                        viewModel = viewModel,
                        isSelectedForNavigation = currentSelectedItemId == item.id,
                        isSelectionModeActive = isSelectionModeActive,
                        isFirstItem = index == 0,
                        onClick = {
                            if (isSelectionModeActive) {
                                viewModel.onItemCheckedChanged(item.id, !item.isSelectedForAction)
                            } else {
                                viewModel.onDrawerItemClick(item.id)
                                onDrawerItemClicked(item.id)
                            }
                        },
                        onLongClick = { viewModel.onItemLongClick(item.id) },
                        onCheckedChanged = { viewModel.onItemCheckedChanged(item.id, it) },
                        onRenameClick = { viewModel.openRenameListDialog(item.id, item.title) },
                        draggableModifier = Modifier.draggableHandle(
                            enabled = index != 0, onDragStopped = { viewModel.saveDrawerItems() })
                    )
                }
            }
        }
    }

    DialogCreateRenameList(
        showDialog = viewModel.showAddListDialog,
        onDismiss = { viewModel.closeAddListDialog() },
        onSave = { viewModel.onConfirmAddNewList(it) },
        title = stringResource(R.string.add_new_list_dialog_title),
        confirmButtonText = stringResource(R.string.save)
    )

    DialogCreateRenameList(
        showDialog = viewModel.showRenameListDialog,
        onDismiss = { viewModel.closeRenameListDialog() },
        onSave = { viewModel.onConfirmRenameList(it) },
        initialName = viewModel.itemToRenameCurrentName,
        title = stringResource(R.string.rename_list_dialog_title),
        confirmButtonText = stringResource(R.string.save)
    )

    DialogDeleteListConfirm(
        showDialog = viewModel.showConfirmDeleteDialog,
        onDismiss = { viewModel.closeConfirmDeleteDialog() },
        onConfirm = { viewModel.onConfirmDeleteSelected() })
}

// Clean, reusable button + divider used in bottomContent
@Composable
private fun ActionButtonWithDivider(
    isSelectionModeActive: Boolean,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val text = if (isSelectionModeActive) stringResource(R.string.delete_lists)
    else stringResource(R.string.add_new_list)

    val containerColor by animateColorAsState(
        if (isSelectionModeActive) extendedMaterialColorScheme.inverseErrorContainer else colorScheme.primary
    )
    val contentColor by animateColorAsState(
        if (isSelectionModeActive) extendedMaterialColorScheme.inverseOnErrorContainer else colorScheme.onPrimary
    )

    var currentButtonPadding by remember { mutableStateOf(NoPadding) }
    val pulsePadding = NoPadding + LargerPadding
    val defaultPadding = NoPadding

    val previousAnyItemSelectedForAction = remember { mutableStateOf(isSelectionModeActive) }


    LaunchedEffect(isSelectionModeActive) {
        if (previousAnyItemSelectedForAction.value != isSelectionModeActive) {
            currentButtonPadding = pulsePadding
            delay(150)
            currentButtonPadding = defaultPadding
            previousAnyItemSelectedForAction.value = isSelectionModeActive
        } else {
            currentButtonPadding = defaultPadding
        }
    }

    val animatedButtonPadding by animateDpAsState(
        targetValue = currentButtonPadding,
        animationSpec = tween(durationMillis = 150),
        label = "Button Padding Pulse Animation"
    )
    Column {
        FilledTonalButton(
            onClick = if (isSelectionModeActive) onDeleteClick else onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor, contentColor = contentColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = animatedButtonPadding)
        ) {
            Text(text = text)
        }
    }
}