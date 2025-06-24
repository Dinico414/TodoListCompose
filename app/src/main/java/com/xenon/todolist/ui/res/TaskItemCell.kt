package com.xenon.todolist.ui.res // Or your actual package for TaskItemCell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.draw.clip
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
import com.xenon.todolist.ui.values.SmallCornerRadius
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.ui.values.SmallMediumPadding
import com.xenon.todolist.ui.values.SmallSpacing
import com.xenon.todolist.ui.values.SmallestCornerRadius
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
        })
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

    val hasDescription = !item.description.isNullOrBlank()
    val hasNotifications = item.notificationCount > 0
    val isHighImportance = item.isHighImportance
    val isHighestImportance = item.isHighestImportance
    val hasSteps = item.stepCount > 0
    val hasAttachments = item.attachmentCount > 0

    val shouldShowDetailsRow = hasDescription || hasNotifications || isHighImportance || isHighestImportance || hasSteps || hasAttachments

    val itemShape = if (shouldShowDetailsRow) {
        RoundedCornerShape(
            topStart = MediumCornerRadius,
            topEnd = MediumCornerRadius,
            bottomStart = SmallestCornerRadius,
            bottomEnd = SmallestCornerRadius
        )
    } else {
        RoundedCornerShape(MediumCornerRadius)
    }


    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SmallSpacing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomAnimatedCheckbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggleCompleted() },
                modifier = Modifier.padding(start = LargePadding, end = LargerPadding),
                enabled = true,
                interactionSource = remember { MutableInteractionSource() })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shadow(SmallElevation, itemShape)
                    .background(Color.Transparent)
                    .clip(itemShape)
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
                    }) {
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
                                    textDecoration = TextDecoration.LineThrough,
                                    color = contentColor
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = contentColor
                                )
                            }, modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp, end = 16.dp)
                        )
                    }
                }
            }
        }

        if (shouldShowDetailsRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 57.dp)
                    .background(
                        containerColor, shape = RoundedCornerShape(
                            topStart = SmallestCornerRadius,
                            topEnd = SmallestCornerRadius,
                            bottomStart = SmallCornerRadius,
                            bottomEnd = SmallCornerRadius
                        )
                    )
                    .padding(
                        top = SmallMediumPadding,
                        bottom = SmallMediumPadding,
                        end = SmallMediumPadding
                    )
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SmallSpacing)
            ) {

                // Notification Icon with Counter
                if (hasNotifications) {
                    IconWithCount(
                        icon = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.task_has_notification),
                        count = item.notificationCount,
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                }

                // Description Icon
                if (hasDescription) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = stringResource(R.string.task_has_description),
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(MaterialTheme.typography.bodyLarge.fontSize.value.dp)
                            .padding(end = SmallSpacing)
                    )
                }

                // HighImportanceIcon
                if (isHighImportance) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = stringResource(R.string.task_is_important),
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(MaterialTheme.typography.bodyLarge.fontSize.value.dp)
                            .padding(end = SmallSpacing)
                    )
                }
                // HighestImportanceIcon
                if (isHighestImportance) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = stringResource(R.string.task_is_very_important),
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(MaterialTheme.typography.bodyLarge.fontSize.value.dp)
                            .padding(end = SmallSpacing)
                    )
                }
                // StepsIcon
                if (hasSteps) {
                    IconWithCount(
                        icon = Icons.Filled.Checklist,
                        contentDescription = stringResource(R.string.task_has_steps),
                        count = item.stepCount,
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                }

                // AttachmentsIcon
                if (hasAttachments) {
                    IconWithCount(
                        icon = Icons.Filled.AttachFile,
                        contentDescription = stringResource(R.string.task_has_attachments),
                        count = item.attachmentCount,
                        tint = contentColor.copy(alpha = 0.7f)
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



@Composable
fun IconWithCount(
    icon: ImageVector,
    contentDescription: String,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(end = SmallSpacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(MaterialTheme.typography.bodyLarge.fontSize.value.dp)
        )
        if (count > 1) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall.copy(color = tint),
                modifier = Modifier.padding(start = SmallSpacing / 2)
            )
        }
    }
}