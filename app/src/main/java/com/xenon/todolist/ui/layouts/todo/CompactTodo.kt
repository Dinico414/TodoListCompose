package com.xenon.todolist.ui.layouts.todo

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.TaskItemCell
import com.xenon.todolist.ui.res.TaskItemContent
import com.xenon.todolist.ui.values.ButtonBoxPadding
import com.xenon.todolist.ui.values.CompactButtonSize
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.NoPadding
import com.xenon.todolist.ui.values.SmallCornerRadius
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.ui.values.SmallPadding
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
            // The FAB is now part of the BottomAppBar
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent, // Make it transparent to see haze effect
                modifier = Modifier
                    .padding(horizontal = LargePadding, vertical = SmallPadding) // Adjust padding as needed
                    .clip(RoundedCornerShape(SmallCornerRadius)) // Rounded corners
                    .hazeEffect( // Apply haze effect
                        state = hazeState,
                        style = HazeMaterials.ultraThin(colorScheme.surface),
                    ),
                contentPadding = PaddingValues(horizontal = SmallPadding) // Inner padding for items
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Sort Button
                    IconButton(onClick = { /* TODO: Implement sort action */ }) {
                        Icon(
                            Icons.Filled.Sort,
                            contentDescription = stringResource(R.string.sort_tasks_description),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }

                    // Filter Button
                    IconButton(onClick = { /* TODO: Implement filter action */ }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = stringResource(R.string.filter_tasks_description),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }

                    // Spacer to push FAB to the center or one side if you prefer
                    Spacer(Modifier.weight(1f))

                    // Existing FAB
                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        containerColor = colorScheme.primary, // Use primary color for FAB
                        elevation = FloatingActionButtonDefaults.elevation(), // Default FAB elevation
                        shape = CircleShape // Circular shape for FAB
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_task_description),
                            tint = colorScheme.onPrimary
                        )
                    }

                    // Spacer to push Settings button to the other side
                    Spacer(Modifier.weight(1f))


                    // Settings Button
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) { scaffoldPadding ->
        ActivityScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
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
                    modifier = Modifier.hazeEffect(
                        state = hazeState, style = HazeMaterials.ultraThin()
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
                            start = LargePadding, end = LargePadding
                        )
                ) {
                    if (todoItems.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_tasks_message),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterHorizontally)
                                .padding(
                                    top = LargePadding,
                                )
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(top = LargePadding)
                        ) {
                            itemsIndexed(
                                items = todoItems,
                                key = { _, item -> item.id }
                            ) { index, item ->
                                val itemId = item.id

                                TaskItemCell(
                                    item = item,
                                    onToggleCompleted = {
                                        viewModel.toggleCompleted(itemId)
                                    },
                                    onDeleteItem = {
                                        viewModel.removeItem(itemId)
                                    },
                                    onEditItem = { updatedTask ->
                                    }
                                )

                                val bottomPadding = if (index == todoItems.lastIndex) {
                                    // Make sure there's enough space for the floating BottomAppBar
                                    WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + LargePadding + 72.dp // Adjust 72.dp based on BottomAppBar height
                                } else {

                                    LargePadding
                                }
                                Spacer(modifier = Modifier.height(bottomPadding))
                            }
                        }
                    }
                }
            })

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    textState = ""
                }, sheetState = sheetState, modifier = Modifier.imePadding()
            ) {
                TaskItemContent(
                    textState = textState,
                    onTextChange = { textState = it },
                    onSaveTask = {
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
                    isSaveEnabled = textState.isNotBlank()
                )
            }
        }
    }
}