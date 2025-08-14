@file:Suppress("UnusedVariable", "unused")

package com.xenon.todolist.ui.res

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.DEFAULT_LIST_ID
import com.xenon.todolist.viewmodel.classes.TodoItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoListCell(
    item: TodoItem,
    isSelectedForNavigation: Boolean,
    isSelectionModeActive: Boolean,
    isFirstItem: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckedChanged: (Boolean) -> Unit,
    onRenameClick: () -> Unit,
    draggableModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    val animationDuration = 300
    val isDefaultItem = item.id == DEFAULT_LIST_ID

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelectedForNavigation) {
            colorScheme.primary.copy(alpha = 0.25f)
        } else {
            colorScheme.primary.copy(alpha = 0.0f)
        },
        animationSpec = tween(durationMillis = animationDuration),
        label = "background color animation"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelectedForNavigation) {
            colorScheme.primary
        } else {
            colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = animationDuration),
        label = "content color animation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(percent = 100))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick, onLongClick = onLongClick
            )
            .padding(vertical = MediumPadding, horizontal = MediumPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MediumPadding)
    ) {
        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = fadeIn(animationSpec = tween(durationMillis = animationDuration)),
            exit = fadeOut(animationSpec = tween(durationMillis = animationDuration))
        ) {
            Checkbox(
                checked = item.isSelectedForAction,
                onCheckedChange = onCheckedChanged,
            )
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            modifier = Modifier.padding(MediumPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = fadeIn(animationSpec = tween(durationMillis = animationDuration)),
            exit = fadeOut(animationSpec = tween(durationMillis = animationDuration))
        ) {
            Row {
                IconButton(onClick = onRenameClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Rename List",
                        tint = contentColor
                    )
                }
                IconButton({}) {
                    Icon(
                        Icons.Rounded.DragHandle,
                        "Reorder",
                        tint = contentColor,
                        modifier = draggableModifier
                    )
                }
//                if (!isFirstItem) {
//                    IconButton(onClick = { /* Handle move options click, possibly with a drag handle */ }) {
//                        Icon(
//                            imageVector = Icons.Filled.DragHandle,
//                            contentDescription = "Move List",
//                            tint = contentColor
//                        )
//                    }
//                }
            }
        }
    }
}