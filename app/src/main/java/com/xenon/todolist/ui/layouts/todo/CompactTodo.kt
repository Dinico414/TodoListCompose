package com.xenon.todolist.ui.layouts.todo

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.TaskItemCell
import com.xenon.todolist.ui.values.ButtonBoxPadding
import com.xenon.todolist.ui.values.CompactButtonSize
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.LargerSpacing
import com.xenon.todolist.ui.values.NoPadding
import com.xenon.todolist.ui.values.SmallCornerRadius
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TodoViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun CompactTodo(
    viewModel: TodoViewModel = viewModel(),
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
) {
    var textState by remember { mutableStateOf("") }
    val todoItems = viewModel.todoItems
    var showMenu by remember { mutableStateOf(false) }

    val isAppBarCollapsible = when (layoutType) {
        LayoutType.COVER -> false
        LayoutType.SMALL -> false
        LayoutType.COMPACT -> !isLandscape
        LayoutType.MEDIUM -> true
        LayoutType.EXPANDED -> true
    }

    val hazeState = rememberHazeState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
            ) {
                Icon(Icons.Filled.Add, stringResource(R.string.add_task_description))
            }
        }
    ) { scaffoldPadding ->

        ActivityScreen(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            title = { fontWeight, fontSize, color ->
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    color = color
                )
            },
            isAppBarCollapsible = isAppBarCollapsible,
            appBarActions = {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .clip(shape = CircleShape)
                        .size(CompactButtonSize)
                        .clickable(
                            onClick = { showMenu = !showMenu },
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = colorScheme.onSurface,
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(x = NoPadding, y = ButtonBoxPadding),
                    containerColor = colorScheme.surfaceContainer.copy(alpha = 0.5f),
                    shadowElevation = SmallElevation,
                    shape = RoundedCornerShape(SmallCornerRadius),
                    modifier = Modifier
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.ultraThin()
                        )
                ) {
                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.settings), color = colorScheme.onSurface
                        )
                    }, onClick = {
                        showMenu = false
                        onOpenSettings()
                    })
                }
            },
            content = { paddingValuesFromAppBar ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = LargePadding,
                            end = LargePadding,
                            top = LargePadding,
                            bottom = WindowInsets.safeDrawing
                                .asPaddingValues()
                                .calculateBottomPadding() + LargePadding
                        )
                ) {

                    if (todoItems.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_tasks_message),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(
                                items = todoItems,
                                key = { item -> item.id }
                            ) { item ->
                                val itemId = item.id
                                TaskItemCell(
                                    item = item,
                                    onToggleCompleted = {
                                        viewModel.toggleCompleted(itemId)
                                    },
                                    onDeleteItem = {
                                        viewModel.removeItem(itemId)
                                    }
                                )
                                Spacer(modifier = Modifier.height(LargerSpacing))
                            }
                        }
                    }
                }
            }
        )
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    textState = ""
                },
                sheetState = sheetState,
                modifier = Modifier
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(LargePadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        label = { Text(stringResource(R.string.new_task_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.addItem(textState)
                                textState = ""
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }
                        },
                        enabled = textState.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.save_task))
                    }
                    Spacer(modifier = Modifier.height(LargePadding))
                }
            }
        }
    }
}