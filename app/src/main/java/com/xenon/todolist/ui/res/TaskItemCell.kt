package com.xenon.todolist.ui.res

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            when (targetValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteItem()
                    true
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    onToggleCompleted()
                    // Keep the item visible after swipe to mark complete
                    false
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        })

    // Reset swipe state after completion toggle to allow re-swiping
    LaunchedEffect(dismissState, item.id) {
        snapshotFlow { dismissState.currentValue }.collect { currentValue ->
            // If the swipe was to mark complete and it's settled back
            if (currentValue == SwipeToDismissBoxValue.StartToEnd && dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                dismissState.reset()
            }
        }
    }

    val isCompleted = item.isCompleted
    val contentColor = if (isCompleted) {
        colorScheme.onSurface.copy(alpha = 0.38f) // Or MaterialTheme.colorScheme.outline
    } else {
        colorScheme.onSecondaryContainer
    }

    val containerColor = if (isCompleted) {
        colorScheme.surfaceVariant.copy(alpha = 0.38f) // Or a less prominent color
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
            onCheckedChange = {
                onToggleCompleted()
            },
            modifier = Modifier.padding(start = LargePadding, end = LargerPadding),
            enabled = true, // Checkbox should always be enabled
            interactionSource = remember { MutableInteractionSource() })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .shadow(SmallElevation, RoundedCornerShape(MediumCornerRadius))
                .background(Color.Transparent) // Shadow needs a non-transparent background on parent if this is transparent
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

                    // Determine background color based on swipe direction
                    val color by animateColorAsState(
                        targetValue = when (targetVal) {
                            SwipeToDismissBoxValue.StartToEnd -> colorScheme.primary // Color for "mark complete"
                            SwipeToDismissBoxValue.EndToStart -> colorScheme.errorContainer // Color for "delete"
                            SwipeToDismissBoxValue.Settled -> Color.Transparent // Or a neutral color
                        }, label = "SwipeBackground"
                    )

                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center // Should not happen with current setup
                    }

                    val iconAsset: ImageVector? = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Check
                        SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                        else -> null // No icon when not swiping or settled
                    }

                    val iconDescription: String? = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> stringResource(R.string.mark_complete_description)
                        SwipeToDismissBoxValue.EndToStart -> stringResource(R.string.delete_task_description)
                        else -> null
                    }

                    // Animate icon scale
                    val scale by animateFloatAsState(
                        targetValue = if (targetVal == SwipeToDismissBoxValue.Settled) 0f else 1f, // Icon visible only when swiping
                        label = "SwipeIconScale",
                        animationSpec = spring(
                            dampingRatio = 0.4f, // Adjust for desired bounciness
                            stiffness = 300f      // Adjust for speed
                        )
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color) // Apply the animated background color
                            .padding(horizontal = 20.dp), // Padding for the icon
                        contentAlignment = alignment
                    ) {
                        if (iconAsset != null && targetVal != SwipeToDismissBoxValue.Settled) {
                            Icon(
                                imageVector = iconAsset,
                                contentDescription = iconDescription,
                                modifier = Modifier.scale(scale),
                                tint = when (targetVal) { // Tint for the icon based on swipe direction
                                    SwipeToDismissBoxValue.StartToEnd -> colorScheme.onPrimary
                                    SwipeToDismissBoxValue.EndToStart -> colorScheme.onErrorContainer
                                    else -> Color.Transparent // Should not be visible if settled
                                }
                            )
                        }
                    }
                }) {
                // This is the content that is swiped
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(containerColor) // Use the conditional container color
                        .clickable { showEditDialog = true }
                        .padding(vertical = 20.dp), // Padding for the text content
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.task,
                        style = if (isCompleted) {
                            MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = contentColor // Use the conditional content color
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                color = contentColor // Use the conditional content color
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp) // Padding for the text itself
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        DialogEditTaskItem( // Assuming this is a composable you've defined elsewhere
            taskItem = item,
            onDismissRequest = { showEditDialog = false },
            onConfirm = { updatedItem ->
                onEditItem(updatedItem)
                showEditDialog = false
            }
        )
    }
}