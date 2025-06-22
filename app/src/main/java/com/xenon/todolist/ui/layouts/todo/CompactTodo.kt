package com.xenon.todolist.ui.layouts.todo

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.TaskItemCell
import com.xenon.todolist.ui.res.TaskItemContent
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TodoViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

// Data class for drawer items
data class DrawerItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CompactTodo(
    viewModel: TodoViewModel = viewModel(),
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
) {
    var textState by remember { mutableStateOf("") }
    val todoItems = viewModel.todoItems

    val isAppBarCollapsible = when (layoutType) {
        LayoutType.COVER -> false
        LayoutType.SMALL -> false
        LayoutType.COMPACT -> !isLandscape
        LayoutType.MEDIUM -> true
        LayoutType.EXPANDED -> true
    }

    val hazeState = rememberHazeState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val drawerItems = listOf(
        DrawerItem("home", "Home", Icons.Filled.Home),
        DrawerItem("tasks", "My Tasks", Icons.Filled.Add),
        DrawerItem("settings_drawer", "Settings", Icons.Filled.Settings)
    )
    var selectedDrawerItemId by remember { mutableStateOf(drawerItems[1].id) }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(LargePadding))

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = LargePadding, vertical = MediumPadding)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MediumPadding),
                    thickness = 2.dp,
                    color = colorScheme.onSurface
                )


                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        item = item, isSelected = selectedDrawerItemId == item.id, onClick = {
                            selectedDrawerItemId = item.id
                            scope.launch { drawerState.close() }
                            Toast.makeText(context, "Navigate to ${item.title}", Toast.LENGTH_SHORT)
                                .show()
                        })
                }
            }
        }) {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues()
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

                                val fabIconTint = colorScheme.onPrimaryContainer
                                val hazeThinColor = colorScheme.primary

                                val smallElevationPx = with(density) { SmallElevation.toPx() }

                                val baseShadowAlpha = 0.7f
                                val interactiveShadowAlpha = 0.9f

                                val currentShadowRadius = if (isPressed || isHovered) {
                                    smallElevationPx * 1.5f
                                } else {
                                    smallElevationPx
                                }

                                val currentShadowAlpha = if (isPressed || isHovered) {
                                    interactiveShadowAlpha
                                } else {
                                    baseShadowAlpha
                                }

                                val currentShadowColor =
                                    colorScheme.scrim.copy(alpha = currentShadowAlpha)
                                val currentYOffsetPx =
                                    with(density) { 1.dp.toPx() }

                                Canvas(
                                    modifier = Modifier.size(
                                        FloatingActionButtonDefaults.LargeIconSize + 24.dp + if (isPressed || isHovered) 8.dp else 5.dp
                                    )
                                ) {
                                    val outline = fabShape.createOutline(
                                        this.size, layoutDirection, density
                                    )
                                    val composePath = Path().apply { addOutline(outline) }

                                    drawIntoCanvas { canvas ->
                                        val frameworkPaint =
                                            androidx.compose.ui.graphics.Paint().asFrameworkPaint()
                                                .apply {
                                                    isAntiAlias = true
                                                    style = android.graphics.Paint.Style.STROKE
                                                    strokeWidth =
                                                        with(this@Canvas) { 0.5.dp.toPx() }
                                                    color = Color.Transparent.toArgb()
                                                    setShadowLayer(
                                                        currentShadowRadius,
                                                        0f, currentYOffsetPx,
                                                        currentShadowColor.toArgb()
                                                    )
                                                }
                                        canvas.nativeCanvas.drawPath(
                                            composePath.asAndroidPath(), frameworkPaint
                                        )
                                    }
                                }
                                FloatingActionButton(
                                    onClick = { showBottomSheet = true },
                                    containerColor = Color.Transparent,
                                    shape = fabShape,
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp,
                                        focusedElevation = 0.dp,
                                        hoveredElevation = 0.dp
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
                                        Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.add_task_description),
                                        tint = fabIconTint
                                    )
                                }
                            }
                        },
                        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(colorScheme.background),
                        contentPadding = FloatingToolbarDefaults.ContentPadding,
                    ) {
                        IconButton(onClick = {
                            Toast.makeText(
                                context, "Coming soon", Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = stringResource(R.string.search_tasks_description),
                                tint = colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = {
                            Toast.makeText(
                                context, "Coming soon", Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.SortByAlpha,
                                contentDescription = stringResource(R.string.sort_tasks_description),
                                tint = colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = {
                            Toast.makeText(
                                context, "Coming soon", Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.FilterAlt,
                                contentDescription = stringResource(R.string.filter_tasks_description),
                                tint = colorScheme.onBackground
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = colorScheme.onBackground
                            )
                        }
                    }
                }
            },
        ) { scaffoldPadding ->
            ActivityScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = scaffoldPadding.calculateTopPadding(),
                        start = scaffoldPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                        end = scaffoldPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                    )
                    .hazeSource(hazeState), title = { fontWeight, fontSize, color ->
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    color = color
                )
            }, isAppBarCollapsible = isAppBarCollapsible, navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                }) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_navigation_menu)
                    )
                }
            }, appBarActions = {}, content = { paddingValuesFromAppBar ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = LargePadding, end = LargePadding
                        )
                ) {
                    if (todoItems.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_tasks_message),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterHorizontally)
                                .padding(
                                    top = LargePadding,
                                )
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f), contentPadding = PaddingValues(
                                top = LargePadding,
                                bottom = scaffoldPadding.calculateBottomPadding() + LargePadding
                            )
                        ) {
                            itemsIndexed(
                                items = todoItems, key = { _, item -> item.id }) { index, item ->
                                val itemId = item.id

                                TaskItemCell(item = item, onToggleCompleted = {
                                    viewModel.toggleCompleted(itemId)
                                }, onDeleteItem = {
                                    viewModel.removeItem(itemId)
                                }, onEditItem = { updatedTask ->
                                })
                                if (index < todoItems.lastIndex) {
                                    Spacer(modifier = Modifier.height(LargePadding))
                                }
                            }
                        }
                    }
                }
            })

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                        textState = ""
                    }, sheetState = sheetState, modifier = Modifier.imePadding()
                ) {
                    TaskItemContent(
                        textState = textState,
                        onTextChange = { textState = it },
                        onSaveTask = {
                            if (textState.isNotBlank()) {
                                viewModel.addItem(textState)
                                textState = ""
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }
                        },
                        isSaveEnabled = textState.isNotBlank()
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerItem(
    item: DrawerItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = LargePadding, vertical = MediumPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MediumPadding)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurface
        )
    }
}
