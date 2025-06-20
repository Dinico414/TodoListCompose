package com.xenon.todolist.ui.res // Ensure this package matches

// import androidx.compose.runtime.LaunchedEffect // Not strictly needed if bounce handles reset
// import androidx.compose.runtime.snapshotFlow    // Not strictly needed if bounce handles reset
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.MediumCornerRadius
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.viewmodel.classes.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemCell(
    item: TodoItem,
    onToggleCompleted: () -> Unit,
    onDeleteItem: () -> Unit,
    onEditItem: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
) {

    var showEditDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            when (targetValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteItem()
                    true
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleCompleted()
                    false
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        }

    )
    val isCompleted = item.isCompleted
    val contentColor = if (isCompleted) {
        colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        colorScheme.onSecondaryContainer
    }

    val containerColor = if (isCompleted) {
        colorScheme.surfaceVariant.copy(alpha = 0.38f)
    } else {
        colorScheme.secondaryContainer
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomAnimatedCheckbox(
            checked = item.isCompleted,
            onCheckedChange = onToggleCompleted,
            modifier = Modifier.padding(start = LargePadding, end = LargerPadding),
            enabled = true,
            interactionSource = remember { MutableInteractionSource() })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .shadow(SmallElevation, RoundedCornerShape(MediumCornerRadius))
                .background(Color.Transparent)
        ) {
            SwipeToDismissBox(
                state = dismissState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    val direction = dismissState.dismissDirection
                    val targetVal = dismissState.targetValue

                    val color by animateColorAsState(
                        targetValue = when (targetVal) {
                            SwipeToDismissBoxValue.StartToEnd -> colorScheme.primary
                            SwipeToDismissBoxValue.EndToStart -> colorScheme.errorContainer
                            else -> Color.Transparent
                        }, label = "SwipeBackground"
                    )

                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center // Should ideally not be hit if always swiping
                    }

                    val iconAsset: ImageVector? = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Check
                        SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                        else -> null
                    }

                    val iconDescription: String? = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> stringResource(R.string.mark_complete_description)
                        SwipeToDismissBoxValue.EndToStart -> stringResource(R.string.delete_task_description)
                        else -> null
                    }

                    val scale by animateFloatAsState(
                        targetValue = if (targetVal == SwipeToDismissBoxValue.Settled) 0f else 1f,
                        label = "SwipeIconScale",
                        animationSpec = spring(
                            dampingRatio = 0.4f, stiffness = 300f
                        )
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        if (iconAsset != null && targetVal != SwipeToDismissBoxValue.Settled) {
                            Icon(
                                imageVector = iconAsset,
                                contentDescription = iconDescription,
                                modifier = Modifier.scale(scale),
                                tint = when (targetVal) {
                                    SwipeToDismissBoxValue.StartToEnd -> colorScheme.onPrimary
                                    SwipeToDismissBoxValue.EndToStart -> colorScheme.onErrorContainer
                                    else -> Color.Transparent
                                }
                            )
                        }
                    }
                }) { // Content of the SwipeToDismissBox
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(containerColor)
                        .clickable { showEditDialog = true }
                        .padding(vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.task, style = if (isCompleted) {
                            MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = TextDecoration.LineThrough, color = contentColor
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                color = contentColor
                            )
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        DialogEditTaskItem(
            taskItem = item,
            onDismissRequest = { showEditDialog = false },
            onConfirm = { updatedItem ->
                onEditItem(updatedItem)
                showEditDialog = false
            })
    }
}
