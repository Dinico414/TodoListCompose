package com.xenonware.todolist.ui.res

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.mylibrary.QuicksandTitleVariable
import com.xenon.mylibrary.values.ExtraLargePadding
import com.xenon.mylibrary.values.LargerCornerRadius
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.MediumPadding
import com.xenon.mylibrary.values.NoPadding
import com.xenon.mylibrary.values.SmallerCornerRadius
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
    devSettingsViewModel: DevSettingsViewModel = viewModel(),
    onDrawerItemClicked: (itemId: String) -> Unit,
) {

    val drawerItems = viewModel.drawerItems
    val currentSelectedItemIdValue = viewModel.selectedDrawerItemId.value
    val isSelectionModeActive = viewModel.isDrawerSelectionModeActive

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val safeDrawingInsets = WindowInsets.safeDrawing.asPaddingValues()

        val startPadding =
            if (safeDrawingInsets.calculateStartPadding(layoutDirection) > 0.dp) NoPadding else MediumPadding
        val topPadding =
            if (safeDrawingInsets.calculateTopPadding() > 0.dp) NoPadding else MediumPadding
        val bottomPadding =
            if (safeDrawingInsets.calculateBottomPadding() > 0.dp) NoPadding else MediumPadding

        val showDummyProfile by devSettingsViewModel.showDummyProfileState.collectAsState()
        val isDeveloperModeEnabled by devSettingsViewModel.devModeToggleState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(
                    start = startPadding, top = topPadding, bottom = bottomPadding
                )
                .clip(
                    RoundedCornerShape(
                        topStart = SmallerCornerRadius,
                        bottomStart = SmallerCornerRadius,
                        topEnd = LargerCornerRadius,
                        bottomEnd = LargerCornerRadius
                    )
                )
                .background(
                    lerp(colorScheme.background, colorScheme.surfaceBright, 0.2f)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ExtraLargePadding)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = stringResource(id = R.string.todo_sheet_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = QuicksandTitleVariable, color = colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = ExtraLargePadding)
                    )


                    if (isDeveloperModeEnabled && showDummyProfile) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            GoogleProfilBorder(
                                modifier = Modifier.size(32.dp),
                            )
                            Image(
                                painter = painterResource(id = R.mipmap.default_icon),
                                contentDescription = stringResource(R.string.open_navigation_menu),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }


                }
                HorizontalDivider(
                    thickness = 1.dp, color = colorScheme.outlineVariant
                )

                val lazyListState = rememberLazyListState()
                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    // Update the list
                    drawerItems.add(to.index, drawerItems.removeAt(from.index))
                }

                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        top = MediumPadding, bottom = MediumPadding
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(drawerItems, key = { _, item -> item.id }) { index, item ->
                        ReorderableItem(
                            reorderableLazyListState,
                            item.id,
                            enabled = index != 0
                        ) { isDragging ->
//                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                            TodoListCell(
                                item = item,
                                viewModel = viewModel,
                                isSelectedForNavigation = currentSelectedItemIdValue == item.id,
                                isSelectionModeActive = isSelectionModeActive,
                                isFirstItem = index == 0,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        viewModel.onItemCheckedChanged(
                                            item.id, !item.isSelectedForAction
                                        )
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
                                draggableModifier = Modifier.draggableHandle(
                                    enabled = index != 0,
                                    onDragStopped = { viewModel.saveDrawerItems() }
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp, color = colorScheme.outlineVariant
                )

                Spacer(Modifier.Companion.height(ExtraLargePadding))

                val buttonText = if (isSelectionModeActive) {
                    stringResource(R.string.delete_lists)
                } else {
                    stringResource(R.string.add_new_list)
                }

                val buttonContainerColor by animateColorAsState(
                    targetValue = if (isSelectionModeActive) {
                        extendedMaterialColorScheme.inverseErrorContainer
                    } else {
                        colorScheme.primary
                    }, label = "Button Container Color Animation"
                )

                val buttonContentColor by animateColorAsState(
                    targetValue = if (isSelectionModeActive) {
                        extendedMaterialColorScheme.inverseOnErrorContainer
                    } else {
                        colorScheme.onPrimary
                    }, label = "Button Content Color Animation"
                )

                var currentButtonPadding by remember { mutableStateOf(NoPadding) }
                val pulsePadding = NoPadding + LargerPadding
                val defaultPadding = NoPadding

                val previousAnyItemSelectedForAction =
                    remember { mutableStateOf(isSelectionModeActive) }


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

                FilledTonalButton(
                    onClick = {
                        if (isSelectionModeActive) {
                            viewModel.openConfirmDeleteDialog()
                        } else {
                            viewModel.openAddListDialog()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainerColor, contentColor = buttonContentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = animatedButtonPadding)
                ) {
                    Text(text = buttonText)
                }
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
        onConfirm = { viewModel.onConfirmDeleteSelected() })
}