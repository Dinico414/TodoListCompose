package com.xenon.todolist.ui.layouts.todo

import android.app.Application
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.TaskItemCell
import com.xenon.todolist.ui.res.TaskItemContent
import com.xenon.todolist.ui.res.TodoListContent
import com.xenon.todolist.ui.values.DialogPadding
import com.xenon.todolist.ui.values.ExtraLargePadding
import com.xenon.todolist.ui.values.ExtraLargeSpacing
import com.xenon.todolist.ui.values.LargePadding
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.ui.values.SmallElevation
import com.xenon.todolist.ui.values.SmallPadding
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TaskViewModel
import com.xenon.todolist.viewmodel.TodoViewModel
import com.xenon.todolist.viewmodel.TodoViewModelFactory
import com.xenon.todolist.viewmodel.classes.Priority
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CompactTodo(
    taskViewModel: TaskViewModel = viewModel(),
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val todoViewModel: TodoViewModel = viewModel(
        factory = TodoViewModelFactory(application, taskViewModel)
    )

    var textState by rememberSaveable { mutableStateOf("") }
    var descriptionState by rememberSaveable { mutableStateOf("") }
    var currentPriority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var selectedDueDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedDueTimeHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedDueTimeMinute by rememberSaveable { mutableStateOf<Int?>(null) }


    val selectedListId by todoViewModel.selectedDrawerItemId

    LaunchedEffect(selectedListId) {
        taskViewModel.currentSelectedListId = selectedListId
    }

    val todoItems = taskViewModel.taskItems

    @Suppress("UnusedVariable", "unused") val isAppBarCollapsible = when (layoutType) {
        LayoutType.COVER -> false
        LayoutType.SMALL -> false
        LayoutType.COMPACT -> !isLandscape
        LayoutType.MEDIUM -> true
        LayoutType.EXPANDED -> true
    }

    val hazeState = rememberHazeState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) {
            todoViewModel.clearAllSelections()
        }
    }

    fun resetBottomSheetState() {
        textState = ""
        descriptionState = ""
        currentPriority = Priority.LOW
        selectedDueDateMillis = null
        selectedDueTimeHour = null
        selectedDueTimeMinute = null
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            TodoListContent(
                viewModel = todoViewModel, onDrawerItemClicked = { _ ->
                    scope.launch { drawerState.close() }
                })
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
                                        resetBottomSheetState()
                                        showBottomSheet = true
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
                                context, "Search coming soon", Toast.LENGTH_SHORT
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
                                context, "Sorting coming soon", Toast.LENGTH_SHORT
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
                                context, "Filter coming soon", Toast.LENGTH_SHORT
                            ).show()
                        }) {
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
            },
        ) { scaffoldPadding ->
            ActivityScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding()
                    .hazeSource(hazeState),
                titleText = stringResource(id = R.string.app_name),
                navigationIconContent = {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_navigation_menu)
                    )
                },
                onNavigationIconClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                },
                appBarActions = {},
                content = { _ ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = ExtraLargeSpacing)
                    ) {
                        if (todoItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_tasks_message),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f), contentPadding = PaddingValues(
                                    top = ExtraLargePadding,
                                    bottom = scaffoldPadding.calculateBottomPadding() + MediumPadding
                                )
                            ) {
                                itemsIndexed(
                                    items = todoItems,
                                    key = { _, item -> item.id }) { index, item ->
                                    TaskItemCell(item = item, onToggleCompleted = {
                                        taskViewModel.toggleCompleted(item.id)
                                    }, onDeleteItem = {
                                        taskViewModel.removeItem(item.id)
                                    }, onEditItem = { updatedTask ->
                                        taskViewModel.updateItem(updatedTask)
                                    })
                                    if (index < todoItems.lastIndex) {
                                        Spacer(modifier = Modifier.height(MediumPadding))
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
                    }, sheetState = sheetState, modifier = Modifier.imePadding()
                ) {
                    TaskItemContent(
                        textState = textState,
                        onTextChange = { textState = it },
                        descriptionState = descriptionState,
                        onDescriptionChange = { descriptionState = it },
                        currentPriority = currentPriority,
                        onPriorityChange = { newPriority -> currentPriority = newPriority },
                        initialDueDateMillis = selectedDueDateMillis,
                        initialDueTimeHour = selectedDueTimeHour,
                        initialDueTimeMinute = selectedDueTimeMinute,
                        onSaveTask = { newDateMillis, newHour, newMinute ->
                            if (textState.isNotBlank()) {
                                taskViewModel.addItem(
                                    task = textState,
                                    description = descriptionState.takeIf { it.isNotBlank() },
                                    priority = currentPriority,
                                    dueDateMillis = newDateMillis,
                                    dueTimeHour = newHour,
                                    dueTimeMinute = newMinute
                                )
                                resetBottomSheetState()
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }
                        },
                        isSaveEnabled = textState.isNotBlank(),
                        horizontalContentPadding = DialogPadding,
                        bottomContentPadding = DialogPadding
                    )
                }
            }
        }
    }
}