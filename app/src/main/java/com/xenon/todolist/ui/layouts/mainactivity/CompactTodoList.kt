package com.xenon.todolist.ui.layouts.mainactivity

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.values.ButtonBoxPadding
import com.xenon.todolist.ui.values.CompactButtonSize
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.NoElevation
import com.xenon.todolist.ui.values.NoPadding
import com.xenon.todolist.ui.values.SmallCornerRadius
import com.xenon.todolist.ui.values.SmallMediumPadding
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
fun CompactTodoList(
    viewModel: TodoViewModel = viewModel(),
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,

    ) {
    var textState by remember { mutableStateOf("") }
    val todoItems = viewModel.todoItems
    var showMenu by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()

    ActivityScreen(title = { fontWeight, fontSize, color ->
        Text(
            text = stringResource(id = R.string.app_name),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = color
        )
    }, appBarActions = {

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
            shadowElevation = NoElevation,
            modifier = Modifier
                .padding(
                    top = SmallMediumPadding, bottom = SmallMediumPadding
                )
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(SmallCornerRadius))
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
                onOpenSettings() // Call the passed lambda
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
                        items = todoItems, key = { item -> item.id }) { item ->
                        TodoItemRow(
                            item = item,
                            onToggleCompleted = { viewModel.toggleCompleted(item) },
                            onDeleteItem = { viewModel.removeItem(item) })
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

@Composable
fun TodoItemRow(
    item: TodoItem,
    onToggleCompleted: () -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted, onCheckedChange = { onToggleCompleted() })
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.task, style = if (item.isCompleted) {
                MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                MaterialTheme.typography.bodyLarge
            }, modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDeleteItem) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_task_description)
            )
        }
    }
}
