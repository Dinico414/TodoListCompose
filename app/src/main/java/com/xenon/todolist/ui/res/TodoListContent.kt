package com.xenon.todolist.ui.res

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.theme.extendedColorScheme
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.TodoViewModel
import kotlinx.coroutines.delay


@Composable
fun TodoListContent(
    viewModel: TodoViewModel,
    onDrawerItemClicked: (itemId: String) -> Unit,
) {

    val drawerItems = viewModel.drawerItems
    val currentSelectedItemIdValue = viewModel.selectedDrawerItemId.value
    val isSelectionModeActive = viewModel.isDrawerSelectionModeActive

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(LargePadding))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = LargePadding, vertical = MediumPadding)
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MediumPadding),
                thickness = 1.dp,
                color = colorScheme.outlineVariant
            )
            Spacer(Modifier.height(MediumPadding))

            drawerItems.forEachIndexed { index, item ->
                TodoListCell(
                    item = item,
                    isSelectedForNavigation = currentSelectedItemIdValue == item.id,
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
                    onLongClick = {
                        viewModel.onItemLongClick(item.id)
                    },
                    onCheckedChanged = { isChecked ->
                        viewModel.onItemCheckedChanged(item.id, isChecked)
                    },
                    onRenameClick = {
                        viewModel.openRenameListDialog(item.id, item.title)
                    },
                    modifier = Modifier.padding(horizontal = LargePadding)
                )
            }

            Spacer(Modifier.weight(1f))

            val buttonText = if (isSelectionModeActive) {
                stringResource(R.string.delete_lists)
            } else {
                stringResource(R.string.add_new_list)
            }

            val buttonContainerColor by animateColorAsState(
                targetValue = if (isSelectionModeActive) {
                    MaterialTheme.extendedColorScheme.inverseErrorContainer
                } else {
                    colorScheme.primary
                },
                label = "Button Container Color Animation"
            )

            val buttonContentColor by animateColorAsState(
                targetValue = if (isSelectionModeActive) {
                    MaterialTheme.extendedColorScheme.inverseOnErrorContainer
                } else {
                    colorScheme.onPrimary
                },
                label = "Button Content Color Animation"
            )

            var currentButtonPadding by remember { mutableStateOf(LargePadding) }
            val pulsePadding = LargePadding + 8.dp
            val defaultPadding = LargePadding

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

            Button(
                onClick = {
                    if (isSelectionModeActive) {
                        viewModel.openConfirmDeleteDialog()
                    } else {
                        viewModel.openAddListDialog()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = animatedButtonPadding)
                    .padding(vertical = LargePadding)
            ) {
                Text(text = buttonText)
            }
        }
    }

    DialogCreateRenameList(
        showDialog = viewModel.showAddListDialog,
        onDismiss = { viewModel.closeAddListDialog() },
        onSave = { listName -> viewModel.onConfirmAddNewList(listName) },
        title = stringResource(R.string.add_new_list_dialog_title),
        confirmButtonText = stringResource(R.string.save)
    )

    DialogCreateRenameList(
        showDialog = viewModel.showRenameListDialog,
        onDismiss = { viewModel.closeRenameListDialog() },
        onSave = { newName -> viewModel.onConfirmRenameList(newName) },
        initialName = viewModel.itemToRenameCurrentName,
        title = stringResource(R.string.rename_list_dialog_title),
        confirmButtonText = stringResource(R.string.save)
    )

    DialogDeleteListConfirm(
        showDialog = viewModel.showConfirmDeleteDialog,
        onDismiss = { viewModel.closeConfirmDeleteDialog() },
        onConfirm = { viewModel.onConfirmDeleteSelected() }
    )
}