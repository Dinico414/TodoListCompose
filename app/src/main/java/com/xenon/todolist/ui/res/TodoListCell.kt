package com.xenon.todolist.ui.res

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xenon.todolist.ui.layouts.todo.DrawerItem
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.MediumPadding

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoListCell(
    item: DrawerItem,
    isSelectedForNavigation: Boolean, // Renamed for clarity
    isSelectionModeActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = LargePadding, vertical = MediumPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MediumPadding)
    ) {
        if (isSelectionModeActive) {
            Checkbox(
                checked = item.isSelectedForAction,
                onCheckedChange = onCheckedChanged
            )
        } else {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelectedForNavigation) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelectedForNavigation && !isSelectionModeActive) colorScheme.primary else colorScheme.onSurface
        )
    }
}