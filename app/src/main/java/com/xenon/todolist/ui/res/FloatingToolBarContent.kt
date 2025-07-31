package com.xenon.todolist.ui.res

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.SmallElevation
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials


@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingToolbarContent(
    hazeState: HazeState,
    onShowBottomSheet: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSortDialog: () -> Unit,
    onOpenFilterDialog: () -> Unit,
    // Add these parameters for search
    onSearchQueryChanged: (String) -> Unit,
    currentSearchQuery: String
) {
    val context = LocalContext.current
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding() + LargePadding,
            ), contentAlignment = Alignment.Center
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            floatingActionButton = {
                Box(contentAlignment = Alignment.Center) {
                    val fabShape = FloatingActionButtonDefaults.shape
                    val density = LocalDensity.current
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    val fabIconTint =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            colorScheme.onPrimaryContainer
                        } else {
                            colorScheme.onPrimary
                        }
                    val hazeThinColor = colorScheme.primary
                    val smallElevationPx = with(density) { SmallElevation.toPx() }
                    val baseShadowAlpha = 0.7f
                    val interactiveShadowAlpha = 0.9f
                    val currentShadowRadius =
                        if (isPressed || isHovered) smallElevationPx * 1.5f else smallElevationPx
                    val currentShadowAlpha =
                        if (isPressed || isHovered) interactiveShadowAlpha else baseShadowAlpha
                    val currentShadowColor =
                        colorScheme.scrim.copy(alpha = currentShadowAlpha)
                    val currentYOffsetPx = with(density) { 1.dp.toPx() }

                    Canvas(
                        modifier = Modifier.size(
                            FloatingActionButtonDefaults.LargeIconSize + 24.dp + if (isPressed || isHovered) 8.dp else 5.dp
                        )
                    ) {
                        val outline =
                            fabShape.createOutline(this.size, layoutDirection, density)
                        val composePath = Path().apply { addOutline(outline) }
                        drawIntoCanvas { canvas ->
                            val frameworkPaint = Paint().asFrameworkPaint().apply {
                                isAntiAlias = true
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = with(this@Canvas) { 0.5.dp.toPx() }
                                color = Color.Transparent.toArgb()
                                setShadowLayer(
                                    currentShadowRadius,
                                    0f,
                                    currentYOffsetPx,
                                    currentShadowColor.toArgb()
                                )
                            }
                            canvas.nativeCanvas.drawPath(
                                composePath.asAndroidPath(), frameworkPaint
                            )
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                onSearchQueryChanged("")
                                keyboardController?.hide()
                            } else {
                                onShowBottomSheet()
                            }
                        },
                        containerColor = Color.Transparent,
                        shape = fabShape,
                        elevation = FloatingActionButtonDefaults.elevation(
                            0.dp, 0.dp, 0.dp, 0.dp
                        ),
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .clip(FloatingActionButtonDefaults.shape)
                            .background(colorScheme.primary)
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.ultraThin(hazeThinColor),
                            )
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (isSearchActive) stringResource(R.string.cancel) else stringResource(R.string.add_task_description),
                            tint = fabIconTint
                        )
                    }
                }
            },
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(colorScheme.background),
            contentPadding = FloatingToolbarDefaults.ContentPadding,
        ) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                TextField(
                    value = currentSearchQuery,
                    onValueChange = {
                        onSearchQueryChanged(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text(stringResource(R.string.placeholder)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                    })
                )
                LaunchedEffect(Unit) {
                    if (isSearchActive) {
                        focusRequester.requestFocus()
                    }
                }
            }

            AnimatedVisibility(
                visible = !isSearchActive,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 })
            ) {
                Row {
                    IconButton(onClick = {
                        isSearchActive = true
                    }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_tasks_description),
                            tint = colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenSortDialog) {
                        Icon(
                            Icons.Filled.SortByAlpha,
                            contentDescription = stringResource(R.string.sort_tasks_description),
                            tint = colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenFilterDialog) {
                        Icon(
                            Icons.Filled.FilterAlt,
                            contentDescription = stringResource(R.string.filter_tasks_description),
                            tint = colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}
