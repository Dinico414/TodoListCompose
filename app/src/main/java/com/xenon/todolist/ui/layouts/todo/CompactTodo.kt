package com.xenon.todolist.ui.layouts.todo

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.TodoItemRow
import com.xenon.todolist.ui.values.ButtonBoxPadding
import com.xenon.todolist.ui.values.CompactButtonSize
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.NoPadding
import com.xenon.todolist.ui.values.SmallCornerRadius
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TodoViewModel
import com.xenon.todolist.viewmodel.classes.TodoItem
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState


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

    ActivityScreen(
        title = { fontWeight, fontSize, color ->
            Text(
                text = stringResource(id = R.string.app_name),
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                color = color
            )
        }, isAppBarCollapsible = isAppBarCollapsible,
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
                containerColor = Color.Transparent,
                shadowElevation = SmallElevation,
                shape = RoundedCornerShape(SmallCornerRadius),
                modifier = Modifier
                    .background(colorScheme.surfaceContainer)
                    .hazeEffect(
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
        }, modifier = Modifier.hazeSource(hazeState), content = { paddingValuesFromAppBar ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = LargePadding,
                        end = LargePadding,
                        top = LargePadding,
                        bottom = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateBottomPadding() + LargePadding
                    )
            ) {
                AddItemInput(text = textState, onTextChange = { textState = it }, onAddItemClick = {
                    if (textState.isNotBlank()) {
                        viewModel.addItem(textState)
                        textState = ""
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
                if (todoItems.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_tasks_message),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(
                            items = todoItems,
                            key = { item -> item.id }
                        ) { item ->
                            val itemId = item.id
                            TodoItemRow(
                                item = item,
                                onToggleCompleted = {
                                    Log.d("CompactTodo", "Calling toggleCompleted for item ID (via lambda): $itemId")
                                    viewModel.toggleCompleted(itemId)
                                },
                                onDeleteItem = {
                                    Log.d("CompactTodo", "Calling removeItem for item ID (via lambda): $itemId")
                                    viewModel.removeItem(itemId)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        })
}

@Composable
fun AddItemInput(
    text: String,
    onTextChange: (String) -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text(stringResource(R.string.new_task_label)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onAddItemClick, enabled = text.isNotBlank()) {
            Icon(
                Icons.Filled.Add, contentDescription = stringResource(R.string.add_task_description)
            )
        }
    }
}
