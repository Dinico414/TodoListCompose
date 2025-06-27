package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.todo.DrawerItem
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.MediumPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListContent(
    drawerItems: List<DrawerItem>,
    selectedDrawerItemId: String,
    onDrawerItemClick: (itemId: String) -> Unit,
    onAddNewListClick: () -> Unit,
    // New parameters for selection mode
    isSelectionModeActive: Boolean,
    onItemLongClick: (itemId: String) -> Unit,
    onItemCheckedChanged: (itemId: String, isChecked: Boolean) -> Unit
) {
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

            drawerItems.forEach { item ->
                TodoListCell(
                    item = item,
                    isSelectedForNavigation = selectedDrawerItemId == item.id,
                    isSelectionModeActive = isSelectionModeActive,
                    onClick = {
                        if (isSelectionModeActive) {
                            // If in selection mode, toggle checkbox
                            onItemCheckedChanged(item.id, !item.isSelectedForAction)
                        } else {
                            // Otherwise, normal navigation
                            onDrawerItemClick(item.id)
                        }
                    },
                    onLongClick = {
                        onItemLongClick(item.id) // Notify CompactTodo to handle long click
                    },
                    onCheckedChanged = { isChecked ->
                        onItemCheckedChanged(item.id, isChecked)
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onAddNewListClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LargePadding)
            ) {
                Text(text = "Add new List")
            }
        }
    }
}