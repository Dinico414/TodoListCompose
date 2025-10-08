@file:Suppress("UnusedVariable", "unused")

package com.xenonware.todolist.ui.res

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xenonware.todolist.ui.values.MediumPadding
import com.xenonware.todolist.viewmodel.TodoViewModel
import com.xenonware.todolist.viewmodel.classes.TodoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoListCell(
    item: TodoItem,
    viewModel: TodoViewModel,
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
    // val isDefaultItem = item.id == DEFAULT_LIST_ID // This variable is unused

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

        val scrollState = rememberScrollState()
        var textWidth = 0
        var rowWidth by remember { mutableIntStateOf(0) }
        val hasOverflow by remember { derivedStateOf { rowWidth < textWidth } }
        val interTextDistance = 40.dp
        val interTextDistancePx = with(LocalDensity.current) { interTextDistance.roundToPx() }
        LaunchedEffect(rowWidth) {
            viewModel.drawerOpenFlow.collectLatest { opened ->
                scrollState.scrollTo(0)
                if (hasOverflow) {
                    delay(1000)
                    scrollState.animateScrollTo(
                        textWidth + interTextDistancePx,
                        tween(durationMillis = textWidth * 10, easing = LinearEasing)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MediumPadding)
                .horizontalFadingEdges(scrollState, fadingEdgeWidth = 30.dp)
                .horizontalScroll(scrollState, enabled = false)
               .onGloballyPositioned { layoutCoordinates ->
                    rowWidth = layoutCoordinates.boundsInParent().width.toInt()
                },
        ) {
            for (i in 0..1) {
                if (i == 1 && !hasOverflow) continue
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (i == 0) {
                                Modifier.onGloballyPositioned { layoutCoordinates ->
                                        textWidth = layoutCoordinates.size.width
                                    }
                            }
                            else Modifier.padding(start = interTextDistance)
                        )
                )
            }
        }


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
                if (!isFirstItem) {
                    IconButton({}) {
                        Icon(
                            Icons.Rounded.DragHandle,
                            "Reorder",
                            tint = contentColor,
                            modifier = draggableModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.horizontalFadingEdges(
    scrollState: ScrollState,
    fadingEdgeWidth: Dp = 20.dp
): Modifier {
    return this
        .graphicsLayer { alpha = 0.99F }
        .drawWithContent {
            drawContent()
            val scrollOffset = scrollState.value
            val maxScrollOffset = scrollState.maxValue

//            if (scrollOffset > 0) {
//                drawRect(
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(Color.Transparent, Color.Black),
//                        startX = 0f,
//                        endX = fadingEdgeWidth.toPx()
//                    ), blendMode = BlendMode.DstIn
//                )
//            }
            if (scrollOffset < maxScrollOffset) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startX = size.width - fadingEdgeWidth.toPx(),
                        endX = size.width
                    ), blendMode = BlendMode.DstIn
                )
            }
        }
}
