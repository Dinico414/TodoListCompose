@file:Suppress("AssignedValueIsNeverRead")

package com.xenonware.todolist.ui.res

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.mylibrary.theme.QuicksandTitleVariable
import com.xenon.mylibrary.values.LargeCornerRadius
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.MediumSpacing
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
import kotlinx.coroutines.delay
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
    modifier: Modifier = Modifier,
    item: TaskItem,
    onToggleCompleted: () -> Unit,
    onDeleteItem: () -> Unit,
    isDragging: Boolean = false,
    viewModel: TaskViewModel = viewModel(),
) {
// ────────────────────────────────────────────────
// 1. Platform / compatibility flags
// ────────────────────────────────────────────────
    val disableOnOldAndroid = remember {
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU
    }

// ────────────────────────────────────────────────
// 2. Core item state & UI helpers
// ────────────────────────────────────────────────
    val isCompleted = item.isCompleted
    val contentColor = if (isCompleted) {
        colorScheme.onSurface.copy(alpha = 0.5f)
    } else {
        colorScheme.onSecondaryContainer
    }

    val defaultContainerColor = if (isCompleted) {
        colorScheme.surfaceContainerHighest
    } else {
        colorScheme.secondaryContainer
    }

    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else SmallElevation, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
        ), label = "card-elevation"
    )

// ────────────────────────────────────────────────
// 3. Haptics, coroutines, deleting state
// ────────────────────────────────────────────────
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }

// ────────────────────────────────────────────────
// 4. Density-dependent thresholds (px values)
// ────────────────────────────────────────────────
    val density = LocalDensity.current

    val dismissThresholdStartToEnd = with(density) { 100.dp.toPx() }
    val dismissThresholdEndToStart = with(density) { 100.dp.toPx() }
    val stretchLimitStartToEnd = with(density) { 120.dp.toPx() }
    val iconVisibleThreshold = with(density) { 50.dp.toPx() }

// ────────────────────────────────────────────────
// 5. Swipe gesture core state & animation
// ────────────────────────────────────────────────
    val offsetX = remember { Animatable(0f) }
    var rawDragOffset by remember { mutableFloatStateOf(0f) }
    var isStuck by remember { mutableStateOf(true) }
    var shapeAndRowOffsetOverride by remember { mutableFloatStateOf(0f) }

    val effectiveOffsetX = if (isDeleting) shapeAndRowOffsetOverride else offsetX.value

    val swipeDirection by remember(offsetX.value) {
        derivedStateOf {
            when {
                offsetX.value > iconVisibleThreshold / 2 -> SwipeDirection.StartToEnd
                offsetX.value < -iconVisibleThreshold / 2 -> SwipeDirection.EndToStart
                else -> SwipeDirection.None
            }
        }
    }

    val swipeProgress by remember(effectiveOffsetX) {
        derivedStateOf {
            val absOffset = abs(effectiveOffsetX)
            val referencePoint = iconVisibleThreshold * 1.6f
            (absOffset / referencePoint).coerceIn(0f, 1f)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = swipeProgress, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh
        ), label = "swipe-progress"
    )

    val startProgress = if (effectiveOffsetX < 0) animatedProgress else 0f
    val endProgress = if (effectiveOffsetX > 0) animatedProgress else 0f

// ────────────────────────────────────────────────
// 6. Swipe visual feedback (alpha, icons, scale)
// ────────────────────────────────────────────────

    val checkAlpha = 1.38f

    val deleteAlpha = 1.38f

    val checkColor = if (isCompleted) colorScheme.tertiary else colorScheme.primary
    val deleteColor = extendedMaterialColorScheme.inverseErrorContainer

    val iconAsset: ImageVector? by remember(swipeDirection) {
        derivedStateOf {
            when (swipeDirection) {
                SwipeDirection.StartToEnd -> if (isCompleted) Icons.Rounded.RadioButtonUnchecked else Icons.Rounded.Check
                SwipeDirection.EndToStart -> Icons.Rounded.Delete
                else -> null
            }
        }
    }

    val onPrimaryColor = if (isCompleted) colorScheme.onTertiary else colorScheme.onPrimary
    val inverseErrorContainerColor = extendedMaterialColorScheme.inverseOnErrorContainer

    val iconTint: Color by remember(swipeDirection) {
        derivedStateOf {
            when (swipeDirection) {
                SwipeDirection.StartToEnd -> onPrimaryColor
                SwipeDirection.EndToStart -> inverseErrorContainerColor
                else -> Color.Transparent
            }
        }
    }

    val iconScale by animateFloatAsState(
        targetValue = when {
            abs(offsetX.value) > iconVisibleThreshold -> 1f
            swipeDirection != SwipeDirection.None -> 0.5f
            else -> 0f
        }, label = "SwipeIconScale", animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f)
    )

// ────────────────────────────────────────────────
// 7. Background gradient (swipe reveal effect)
// ────────────────────────────────────────────────
    val backgroundBrush by remember(swipeDirection, isDeleting, checkAlpha, deleteAlpha) {
        derivedStateOf {
            if (isDeleting) {
                Brush.horizontalGradient(
                    0f to deleteColor, 1f to deleteColor
                )
            } else when (swipeDirection) {
                SwipeDirection.StartToEnd -> Brush.horizontalGradient(
                    0.00f to checkColor.copy(alpha = checkAlpha),
                    0.42f to checkColor.copy(alpha = checkAlpha * 0.88f),
                    0.50f to checkColor.copy(alpha = checkAlpha * 0.12f),
                    0.58f to Color.Transparent,
                    1.00f to Color.Transparent
                )

                SwipeDirection.EndToStart -> Brush.horizontalGradient(
                    0.00f to Color.Transparent,
                    0.42f to Color.Transparent,
                    0.50f to deleteColor.copy(alpha = deleteAlpha * 0.12f),
                    0.58f to deleteColor.copy(alpha = deleteAlpha * 0.88f),
                    1.00f to deleteColor.copy(alpha = deleteAlpha)
                )

                else -> Brush.horizontalGradient(
                    0f to Color.Transparent, 1f to Color.Transparent
                )
            }
        }
    }

// ────────────────────────────────────────────────
// 8. Details row visibility & metadata flags
// ────────────────────────────────────────────────
    val hasDescription = !item.description.isNullOrBlank()
    val hasNotifications = item.notificationCount > 0
    val isHighImportance = item.isHighImportance
    val isHighestImportance = item.isHighestImportance

    val hasSteps by remember(item.steps) { derivedStateOf { item.steps.isNotEmpty() } }
    val completedStepsCount by remember(item.steps) { derivedStateOf { item.steps.count { it.isCompleted } } }
    val totalStepsCount by remember(item.steps) { derivedStateOf { item.steps.size } }

    val hasAttachments = item.attachmentCount > 0

    val shouldShowDetailsRow =
        hasDescription || hasNotifications || isHighImportance || isHighestImportance || hasSteps || hasAttachments

// ────────────────────────────────────────────────
// 9. Corner radius animations (main card + details row)
// ────────────────────────────────────────────────
    val bottomStartRadius = if (shouldShowDetailsRow) {
        if (!disableOnOldAndroid) SmallestCornerRadius + (LargeCornerRadius - SmallestCornerRadius) * endProgress
        else SmallestCornerRadius
    } else {
        LargeCornerRadius
    }

    val bottomEndRadius = if (shouldShowDetailsRow) {
        if (!disableOnOldAndroid) SmallestCornerRadius + (LargeCornerRadius - SmallestCornerRadius) * startProgress
        else SmallestCornerRadius
    } else {
        LargeCornerRadius
    }

    val detailsTopStartRadius = if (shouldShowDetailsRow && !disableOnOldAndroid) {
        SmallestCornerRadius + (SmallCornerRadius - SmallestCornerRadius) * endProgress
    } else {
        SmallestCornerRadius
    }

    val detailsTopEndRadius = if (shouldShowDetailsRow && !disableOnOldAndroid) {
        SmallestCornerRadius + (SmallCornerRadius - SmallestCornerRadius) * startProgress
    } else {
        SmallestCornerRadius
    }

// ────────────────────────────────────────────────
// 10. Shape definitions
// ────────────────────────────────────────────────
    val mainContentShape = RoundedCornerShape(
        topStart = LargeCornerRadius,
        topEnd = LargeCornerRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius
    )

    val detailsRowShape = RoundedCornerShape(
        topStart = detailsTopStartRadius,
        topEnd = detailsTopEndRadius,
        bottomStart = SmallCornerRadius,
        bottomEnd = SmallCornerRadius
    )

// ────────────────────────────────────────────────
// 11. Squeeze / inset animations for details row
// ────────────────────────────────────────────────
    val animatedShortenStart by animateDpAsState(
        targetValue = if (shouldShowDetailsRow && !disableOnOldAndroid) 14.dp * endProgress else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh
        ),
        label = "shorten-start"
    )

    val animatedShortenEnd by animateDpAsState(
        targetValue = if (shouldShowDetailsRow && !disableOnOldAndroid) 14.dp * startProgress else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh
        ),
        label = "shorten-end"
    )

    val animatedIcon by animateDpAsState(
        targetValue = if (shouldShowDetailsRow && !disableOnOldAndroid) {
            6.dp * if (startProgress > 0) startProgress else endProgress
        } else 0.dp, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh
        ), label = "animated-icon"
    )

// ────────────────────────────────────────────────
// 12. Due date / time formatting
// ────────────────────────────────────────────────
    val calendar = remember { Calendar.getInstance() }

    val dateFormatter = remember {
        JavaDateFormat.getDateInstance(JavaDateFormat.SHORT, Locale.getDefault())
    }

    val timeFormatter = remember {
        JavaDateFormat.getTimeInstance(JavaDateFormat.SHORT, Locale.getDefault())
    }

    val formattedDate: String? = remember(item.dueDateMillis) {
        item.dueDateMillis?.let { dateFormatter.format(it) }
    }

    val formattedTime: String? = remember(item.dueTimeHour, item.dueTimeMinute) {
        if (item.dueTimeHour != null && item.dueTimeMinute != null) {
            calendar.set(Calendar.HOUR_OF_DAY, item.dueTimeHour)
            calendar.set(Calendar.MINUTE, item.dueTimeMinute)
            timeFormatter.format(calendar.time)
        } else null
    }

// ────────────────────────────────────────────────
// 13. Animation specs reused later
// ────────────────────────────────────────────────
    val snapBackSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    )

    LaunchedEffect(item.id) {
        if (offsetX.value != 0f) {
            offsetX.snapTo(0f)
        }
        isDeleting = false
        shapeAndRowOffsetOverride = 0f
    }

    var previousDragging by remember { mutableStateOf(false) }
    LaunchedEffect(isDragging) {
        if (isDragging && !previousDragging) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else if (!isDragging && previousDragging) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(200)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previousDragging = isDragging
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bottomRowHeight = SmallPadding * 2 + SmallSpacing + 18.dp
        CustomAnimatedCheckbox(
            checked = item.isCompleted, onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleCompleted()

                if (!disableOnOldAndroid) {
                    coroutineScope.launch {
                        delay(100)
                        val pulseTargetPx = with(density) { 10.dp.toPx() }
                        offsetX.animateTo(
                            targetValue = pulseTargetPx, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessHigh
                            )
                        )
                        offsetX.animateTo(
                            targetValue = 0f, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }
            }, modifier = Modifier.padding(
                end = LargerPadding, bottom = if (shouldShowDetailsRow) bottomRowHeight else 0.dp
            ), enabled = true, interactionSource = remember { MutableInteractionSource() })

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation, mainContentShape, clip = false)
                    .clip(mainContentShape)
                    .background(defaultContainerColor)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(backgroundBrush, mainContentShape)
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
                            onDragStarted = {
                                rawDragOffset = offsetX.value
                                isStuck = abs(rawDragOffset) < dismissThresholdStartToEnd * 1.0f
                            },
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    val unstickDist = dismissThresholdStartToEnd * 1.0f
                                    val restickDist = dismissThresholdStartToEnd * 0.85f

                                    var newRawDrag = rawDragOffset + delta

                                    val newStuck = if (isStuck) {
                                        abs(newRawDrag) < unstickDist
                                    } else {
                                        abs(newRawDrag) < restickDist
                                    }

                                    if (newStuck != isStuck) {
                                        if (newStuck) {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                        }
                                        isStuck = newStuck
                                    }

                                    val friction = 1.8f
                                    val effectiveLimitRight = stretchLimitStartToEnd * if (isStuck) friction else 1f
                                    val effectiveLimitLeft = stretchLimitStartToEnd * if (isStuck) friction else 1f

                                    newRawDrag = newRawDrag.coerceIn(-effectiveLimitLeft, effectiveLimitRight)
                                    rawDragOffset = newRawDrag

                                    val intendedOffset = rawDragOffset / if (isStuck) friction else 1f

                                    val targetDrag = if (intendedOffset > 0) {
                                        applyStretch(
                                            offset = intendedOffset,
                                            threshold = dismissThresholdStartToEnd,
                                            stretchFactor = 1f
                                        ).coerceIn(0f, stretchLimitStartToEnd)
                                    } else {
                                        applyStretch(
                                            offset = intendedOffset,
                                            threshold = dismissThresholdEndToStart,
                                            stretchFactor = 1f
                                        ).coerceIn(-stretchLimitStartToEnd, 0f)
                                    }

                                    offsetX.animateTo(
                                        targetValue = targetDrag,
                                        animationSpec = spring(
                                            dampingRatio = 0.65f,
                                            stiffness = 1500f
                                        )
                                    )
                                }
                            },
                            onDragStopped = { velocity ->
                                coroutineScope.launch {
                                    val isDismissRight = velocity >= 4000f || (velocity >= 0f && !isStuck && rawDragOffset > 0f)
                                    val isDismissLeft = velocity <= -4000f || (velocity <= 0f && !isStuck && rawDragOffset < 0f)

                                    if (isDismissRight) {
                                        if (isStuck) {
                                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        onToggleCompleted()
                                        offsetX.animateTo(
                                            targetValue = 0f, animationSpec = snapBackSpring
                                        )
                                    } else if (isDismissLeft) {
                                        if (isStuck) {
                                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        isDeleting = true
                                        shapeAndRowOffsetOverride = offsetX.value

                                        coroutineScope.launch {
                                            val overrideAnim = Animatable(shapeAndRowOffsetOverride)
                                            overrideAnim.animateTo(
                                                targetValue = 0f, 
                                                animationSpec = spring(
                                                    dampingRatio = 0.35f,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            ) {
                                                shapeAndRowOffsetOverride = value
                                            }
                                        }

                                        val screenWidthPx = with(density) { 400.dp.toPx() }
                                        val offScreenTarget = -screenWidthPx * 1.5f

                                        offsetX.animateTo(
                                            targetValue = offScreenTarget,
                                            animationSpec = tween(
                                                durationMillis = 280,
                                                easing = CubicBezierEasing(0.4f, -0.15f, 0.2f, 1f)
                                            )
                                        )

                                        onDeleteItem()
                                    } else {
                                        offsetX.animateTo(
                                            targetValue = 0f, animationSpec = snapBackSpring
                                        )
                                    }

                                    rawDragOffset = 0f
                                    isStuck = true
                                }
                            })
                        .fillMaxSize()
                        .background(defaultContainerColor, mainContentShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (abs(offsetX.value) < with(density) { 5.dp.toPx() }) {
                                viewModel.showTaskSheetForEdit(item)
                            }
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.task,
                        style = if (isCompleted) {
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = QuicksandTitleVariable, color = contentColor
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = QuicksandTitleVariable,
                                color = contentColor
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 20.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    )

                    if (formattedDate != null || formattedTime != null) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(SmallPadding),
                            modifier = Modifier.padding(end = LargerPadding)
                        ) {
                            if (formattedTime != null) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = QuicksandTitleVariable, fontSize = 14.sp
                                    ),
                                    color = contentColor,
                                    textAlign = TextAlign.End
                                )
                            }
                            if (formattedDate != null) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = QuicksandTitleVariable, fontSize = 14.sp
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
                        .padding(
                            start = animatedShortenStart.coerceAtLeast(0.dp),
                            end = animatedShortenEnd.coerceAtLeast(0.dp)
                        )
                        .shadow(elevation, detailsRowShape, clip = false)
                        .clip(detailsRowShape)
                        .background(defaultContainerColor)
                        .padding(
                            top = SmallPadding,
                            bottom = SmallPadding,
                            start = 16.dp - animatedIcon,
                            end = SmallMediumPadding
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MediumSpacing)
                ) {
                    if (hasNotifications) {
                        IconWithCount(
                            icon = Icons.Rounded.Notifications,
                            contentDescription = stringResource(R.string.task_has_notification),
                            count = item.notificationCount,
                            tint = contentColor,
                            iconSize = iconSizeDp,
                            modifier = Modifier.padding(end = MediumSpacing)
                        )
                    }
                    if (hasDescription) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = stringResource(R.string.task_has_description),
                            tint = contentColor,
                            modifier = Modifier
                                .size(iconSizeDp)
                                .padding(end = MediumSpacing)
                        )
                    }
                    if (isHighImportance && !isHighestImportance) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = stringResource(R.string.task_is_important),
                            tint = contentColor,
                            modifier = Modifier
                                .size(iconSizeDp)
                                .padding(end = MediumSpacing)
                        )
                    }
                    if (isHighestImportance) {
                        Icon(
                            imageVector = Icons.Rounded.Error,
                            contentDescription = stringResource(R.string.task_is_very_important),
                            tint = contentColor,
                            modifier = Modifier
                                .size(iconSizeDp)
                                .padding(end = MediumSpacing)
                        )
                    }
                    if (hasSteps) {
                        IconWithStepsCount(
                            icon = Icons.Rounded.Checklist,
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
                            icon = Icons.Rounded.AttachFile,
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
                text = count.toString(), style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = QuicksandTitleVariable, color = tint
                ), modifier = Modifier.padding(start = SmallSpacing / 2)
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
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
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
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = QuicksandTitleVariable, color = tint
                ),
                modifier = Modifier.padding(start = SmallSpacing / 2)
            )
        }
    }
}