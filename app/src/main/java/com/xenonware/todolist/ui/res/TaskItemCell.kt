package com.xenonware.todolist.ui.res

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.mylibrary.values.LargeCornerRadius
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.LargerSpacing
import com.xenon.mylibrary.values.MediumSpacing
import com.xenon.mylibrary.values.NoCornerRadius
import com.xenon.mylibrary.values.SmallCornerRadius
import com.xenon.mylibrary.values.SmallElevation
import com.xenon.mylibrary.values.SmallMediumPadding
import com.xenon.mylibrary.values.SmallPadding
import com.xenon.mylibrary.values.SmallSpacing
import com.xenon.mylibrary.values.SmallestCornerRadius
import com.xenonware.todolist.R
import com.xenonware.todolist.ui.theme.extendedMaterialColorScheme
import com.xenonware.todolist.viewmodel.TaskViewModel
import com.xenonware.todolist.viewmodel.classes.TaskItem
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import java.text.DateFormat as JavaDateFormat


enum class SwipeDirection {
    StartToEnd, EndToStart, None
}

fun applyStretch(offset: Float, threshold: Float, stretchFactor: Float = 0.5f): Float {
    val sign = offset.sign
    val absOffset = abs(offset)

    if (absOffset <= threshold) {
        return offset
    }

    val overscroll = absOffset - threshold
    val stretchedOverscroll = overscroll.pow(1f - stretchFactor)
    return sign * (threshold + stretchedOverscroll)
}


@Composable
fun TaskItemCell(
    item: TaskItem,
    onToggleCompleted: () -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val haptic = LocalHapticFeedback.current
    val isCompleted = item.isCompleted
    val coroutineScope = rememberCoroutineScope()

    val contentColor = if (isCompleted) {
        colorScheme.onSurface.copy(alpha = 0.5f)
    } else {
        colorScheme.onSurface
    }


    val defaultContainerColor = if (isCompleted) {
      colorScheme.surfaceContainerHighest
    } else {
        colorScheme.secondaryContainer
    }


    val density = LocalDensity.current
    val dismissThresholdStartToEnd = with(density) { 100.dp.toPx() }
    val dismissThresholdEndToStart = with(density) { 100.dp.toPx() }

    val stretchLimitStartToEnd = with(density) { 150.dp.toPx() }
    val iconVisibleThreshold = with(density) { 50.dp.toPx() }

    val offsetX = remember { Animatable(0f) }

    val swipeDirection by remember(offsetX.value) {
        derivedStateOf {
            when {
                offsetX.value > iconVisibleThreshold / 2 -> SwipeDirection.StartToEnd
                offsetX.value < -iconVisibleThreshold / 2 -> SwipeDirection.EndToStart
                else -> SwipeDirection.None
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (swipeDirection) {
            SwipeDirection.StartToEnd -> colorScheme.primary
            SwipeDirection.EndToStart -> extendedMaterialColorScheme.inverseErrorContainer
            SwipeDirection.None -> defaultContainerColor
        }, label = "SwipeBackground"
    )

    val iconAsset: ImageVector? by remember(swipeDirection) {
        derivedStateOf {
            when (swipeDirection) {
                SwipeDirection.StartToEnd -> Icons.Filled.Check
                SwipeDirection.EndToStart -> Icons.Filled.Delete
                else -> null
            }
        }
    }
    val onPrimaryColor = colorScheme.onPrimary
    val inverseErrorContainerColor = extendedMaterialColorScheme.inverseOnErrorContainer

    val iconTint: Color by remember(swipeDirection, onPrimaryColor, inverseErrorContainerColor) {
        derivedStateOf {
            when (swipeDirection) {
                SwipeDirection.StartToEnd -> onPrimaryColor
                SwipeDirection.EndToStart -> inverseErrorContainerColor
                else -> Color.Transparent
            }
        }
    }

    val iconScale by animateFloatAsState(
        targetValue = if (abs(offsetX.value) > iconVisibleThreshold) 1f else if (swipeDirection != SwipeDirection.None) 0.5f else 0f,
        label = "SwipeIconScale",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f)
    )

    val hasDescription = !item.description.isNullOrBlank()
    val hasNotifications = item.notificationCount > 0

    val isHighImportance = item.isHighImportance
    val isHighestImportance = item.isHighestImportance

    val hasSteps by remember(item.steps) { derivedStateOf { item.steps.isNotEmpty() } }
    val completedStepsCount by remember(item.steps) {
        derivedStateOf { item.steps.count { it.isCompleted } }
    }
    val totalStepsCount by remember(item.steps) {
        derivedStateOf { item.steps.size }
    }

    val hasAttachments = item.attachmentCount > 0

    val shouldShowDetailsRow =
        hasDescription || hasNotifications || isHighImportance || isHighestImportance || hasSteps || hasAttachments

    val mainContentShape = RoundedCornerShape(
        topStart = LargeCornerRadius,
        topEnd = LargeCornerRadius,
        bottomStart = if (shouldShowDetailsRow) SmallestCornerRadius else LargeCornerRadius,
        bottomEnd = if (shouldShowDetailsRow) SmallestCornerRadius else LargeCornerRadius
    )
    val swipeToDismissShape = RoundedCornerShape(
        topStart = LargeCornerRadius,
        topEnd = LargeCornerRadius,
        bottomStart = if (shouldShowDetailsRow) NoCornerRadius else LargeCornerRadius,
        bottomEnd = if (shouldShowDetailsRow) NoCornerRadius else LargeCornerRadius
    )

    val detailsRowShape = RoundedCornerShape(
        topStart = SmallestCornerRadius,
        topEnd = SmallestCornerRadius,
        bottomStart = SmallCornerRadius,
        bottomEnd = SmallCornerRadius
    )

    val calendar = remember { Calendar.getInstance() }
    val dateFormatter =
        remember { JavaDateFormat.getDateInstance(JavaDateFormat.SHORT, Locale.getDefault()) }
    val timeFormatter =
        remember { JavaDateFormat.getTimeInstance(JavaDateFormat.SHORT, Locale.getDefault()) }

    val formattedDate: String? = remember(item.dueDateMillis) {
        item.dueDateMillis?.let { dateFormatter.format(it) }
    }

    val formattedTime: String? = remember(item.dueTimeHour, item.dueTimeMinute) {
        if (item.dueTimeHour != null && item.dueTimeMinute != null) {
            calendar.set(Calendar.HOUR_OF_DAY, item.dueTimeHour)
            calendar.set(Calendar.MINUTE, item.dueTimeMinute)
            timeFormatter.format(calendar.time)
        } else {
            null
        }
    }


    LaunchedEffect(item.id) {
        if (offsetX.value != 0f) {
            offsetX.snapTo(0f)
        }
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
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleCompleted()
            },
            modifier = Modifier.padding(end = LargerPadding),
            enabled = true,
            interactionSource = remember { MutableInteractionSource() })

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(SmallElevation, mainContentShape, clip = false)
                    .clip(mainContentShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(backgroundColor, mainContentShape)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (offsetX.value > 0) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (iconAsset != null && swipeDirection != SwipeDirection.None) {
                        Icon(
                            imageVector = iconAsset!!, contentDescription = when (swipeDirection) {
                                SwipeDirection.StartToEnd -> stringResource(if (isCompleted) R.string.mark_incomplete_description else R.string.mark_complete_description)
                                SwipeDirection.EndToStart -> stringResource(R.string.delete_task_description)
                                else -> null
                            }, modifier = Modifier.scale(iconScale), tint = iconTint
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.value.toInt(), 0) }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    val targetDrag = offsetX.value + delta
                                    val newOffset = if (targetDrag > 0) {
                                        applyStretch(
                                            offset = targetDrag,
                                            threshold = dismissThresholdStartToEnd,
                                            stretchFactor = 1f
                                        ).coerceIn(0f, stretchLimitStartToEnd)
                                    } else {
                                        applyStretch(
                                            offset = targetDrag,
                                            threshold = dismissThresholdEndToStart,
                                            stretchFactor = 1f
                                        ).coerceIn(-stretchLimitStartToEnd, 0f)
                                    }
                                    offsetX.snapTo(newOffset)
                                }
                            },
                            onDragStopped = {
                                coroutineScope.launch {
                                    val currentOffset = offsetX.value
                                    if (currentOffset > dismissThresholdStartToEnd) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onToggleCompleted()
                                        offsetX.animateTo(
                                            0f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    } else if (currentOffset < -dismissThresholdEndToStart) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDeleteItem()
                                    } else {
                                        offsetX.animateTo(
                                            0f,
                                            animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                        )
                                    }
                                }
                            })
                        .fillMaxSize()
                        .background(defaultContainerColor, swipeToDismissShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (abs(offsetX.value) < with(density) { 5.dp.toPx() }) {
                                viewModel.showTaskSheetForEdit(item)
                            }
                        }
                        .padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.task, style = if (isCompleted) {
                            MaterialTheme.typography.bodyLarge.copy(
                                color = contentColor
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(color = contentColor)
                        }, modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 16.dp)
                    )
                    if (formattedDate != null || formattedTime != null) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = LargerSpacing)
                        ) {
                            if (formattedDate != null) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 16.sp
                                    ),
                                    color = contentColor,
                                    textAlign = TextAlign.End
                                )
                            }
                            if (formattedTime != null) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 16.sp
                                    ),
                                    color = contentColor,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            if (shouldShowDetailsRow) {
                Spacer(modifier = Modifier.height(SmallSpacing))

                val iconSizeDp = 18.dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(SmallElevation, detailsRowShape, clip = false)
                        .clip(detailsRowShape)
                        .background(defaultContainerColor)
                        .padding(
                            top = SmallPadding,
                            bottom = SmallPadding,
                            end = SmallMediumPadding,
                            start = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MediumSpacing)
                ) {

                    if (hasNotifications) {
                        IconWithCount(
                            icon = Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.task_has_notification),
                            count = item.notificationCount,
                            tint = contentColor,
                            iconSize = iconSizeDp,
                            modifier = Modifier.padding(end = MediumSpacing)
                        )
                    }
                    if (hasDescription) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = stringResource(R.string.task_has_description),
                            tint = contentColor,
                            modifier = Modifier
                                .size(iconSizeDp)
                                .padding(end = MediumSpacing)
                        )
                    }
                    if (isHighImportance && !isHighestImportance) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = stringResource(R.string.task_is_important),
                            tint = contentColor,
                            modifier = Modifier
                                .size(iconSizeDp)
                                .padding(end = MediumSpacing)
                        )
                    }
                    if (isHighestImportance) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = stringResource(R.string.task_is_very_important),
                            tint = contentColor,
                            modifier = Modifier
                                .size(iconSizeDp)
                                .padding(end = MediumSpacing)
                        )
                    }
                    if (hasSteps) {
                        IconWithStepsCount(
                            icon = Icons.Filled.Checklist,
                            contentDescription = stringResource(R.string.task_has_steps),
                            completedCount = completedStepsCount,
                            totalCount = totalStepsCount,
                            tint = contentColor,
                            iconSize = iconSizeDp,
                            modifier = Modifier.padding(end = MediumSpacing)
                        )
                    }
                    if (hasAttachments) {
                        IconWithCount(
                            icon = Icons.Filled.AttachFile,
                            contentDescription = stringResource(R.string.task_has_attachments),
                            count = item.attachmentCount,
                            tint = contentColor,
                            iconSize = iconSizeDp,
                            modifier = Modifier.padding(end = MediumSpacing)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconWithCount(
    icon: ImageVector,
    contentDescription: String,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 26.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
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
@Composable
fun IconWithStepsCount(
    icon: ImageVector,
    contentDescription: String,
    completedCount: Int,
    totalCount: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 26.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
        if (totalCount > 0) {
            Text(
                text = "$completedCount/$totalCount",
                style = MaterialTheme.typography.bodySmall.copy(color = tint),
                modifier = Modifier.padding(start = SmallSpacing / 2)
            )
        }

    }
}