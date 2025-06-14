package com.xenon.todolist.ui.res

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring // Import spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn // Import heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.MediumCornerRadius
import com.xenon.todolist.viewmodel.classes.TodoItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItemRow(
    item: TodoItem,
    onToggleCompleted: () -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            when (targetValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteItem()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onToggleCompleted()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        })

    LaunchedEffect(dismissState, item.id) {
        snapshotFlow { dismissState.currentValue }.collect { currentValue ->
            if (currentValue == SwipeToDismissBoxValue.StartToEnd && dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                dismissState.reset()
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = {
                onToggleCompleted()
            },
        )
        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(MediumCornerRadius))
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
                            SwipeToDismissBoxValue.Settled -> colorScheme.secondaryContainer
                        }, label = "SwipeBackground"
                    )

                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
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
                        animationSpec = spring( // Add spring animationSpec
                            dampingRatio = 0.4f, // Adjust for more or less bounce
                            stiffness = 300f    // Adjust for speed of bounce
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
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.secondaryContainer)
                        .padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.task, style = if (item.isCompleted) {
                            MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                color = colorScheme.onSecondaryContainer
                            )
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )
                }
            }
        }
    }
}