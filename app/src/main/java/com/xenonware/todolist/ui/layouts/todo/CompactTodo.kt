package com.xenonware.todolist.ui.layouts.todo

//import com.xenon.mylibrary.res.FloatingToolbarContent
import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.mylibrary.ActivityScreen
import com.xenon.mylibrary.QuicksandTitleVariable
import com.xenon.mylibrary.res.FloatingToolbarContent
import com.xenonware.todolist.R
import com.xenonware.todolist.ui.res.DialogTaskItemFiltering
import com.xenonware.todolist.ui.res.DialogTaskItemSorting
import com.xenonware.todolist.ui.res.GoogleProfilBorder
import com.xenonware.todolist.ui.res.TaskItemCell
import com.xenonware.todolist.ui.res.TaskItemContent
import com.xenonware.todolist.ui.res.TodoListContent
import com.xenonware.todolist.ui.res.XenonSnackbar
import com.xenonware.todolist.ui.values.DialogPadding
import com.xenonware.todolist.ui.values.ExtraLargePadding
import com.xenonware.todolist.ui.values.ExtraLargeSpacing
import com.xenonware.todolist.ui.values.LargestPadding
import com.xenonware.todolist.ui.values.MediumPadding
import com.xenonware.todolist.ui.values.MediumSpacing
import com.xenonware.todolist.ui.values.SmallPadding
import com.xenonware.todolist.viewmodel.DevSettingsViewModel
import com.xenonware.todolist.viewmodel.LayoutType
import com.xenonware.todolist.viewmodel.SnackbarEvent
import com.xenonware.todolist.viewmodel.TaskViewModel
import com.xenonware.todolist.viewmodel.TodoViewModel
import com.xenonware.todolist.viewmodel.TodoViewModelFactory
import com.xenonware.todolist.viewmodel.classes.Priority
import com.xenonware.todolist.viewmodel.classes.TaskItem
import com.xenonware.todolist.viewmodel.classes.TaskStep
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CompactTodo(
    taskViewModel: TaskViewModel = viewModel(),
    devSettingsViewModel: DevSettingsViewModel = viewModel(),
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
    appSize: IntSize,
    ) {
    val application = LocalContext.current.applicationContext as Application
    val todoViewModel: TodoViewModel = viewModel(
        factory = TodoViewModelFactory(application, taskViewModel)
    )

    var editingTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
    var textState by rememberSaveable { mutableStateOf("") }
    var descriptionState by rememberSaveable { mutableStateOf("") }
    var currentPriority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var selectedDueDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedDueTimeHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedDueTimeMinute by rememberSaveable { mutableStateOf<Int?>(null) }
    val currentSteps = remember { mutableStateListOf<TaskStep>() }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }


    val selectedListId by todoViewModel.selectedDrawerItemId

    LaunchedEffect(selectedListId) {
        taskViewModel.currentSelectedListId = selectedListId
    }

    val todoItemsWithHeaders = taskViewModel.taskItems

    val coverLayout = layoutType == LayoutType.COVER

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val density = LocalDensity.current
    val appWidthDp = with(density) { appSize.width.toDp() }
    val appHeightDp = with(density) { appSize.height.toDp() }

    val currentAspectRatio = if (isLandscape) {
        appWidthDp / appHeightDp
    } else {
        appHeightDp / appWidthDp
    }

    val aspectRatioConditionMet = if (isLandscape) {
        currentAspectRatio > 0.5625f
    } else {
        currentAspectRatio < 1.77f
    }

    val isAppBarCollapsible = when (layoutType) {
        LayoutType.COVER -> false
        LayoutType.SMALL -> false
        LayoutType.COMPACT -> !isLandscape || !aspectRatioConditionMet
        LayoutType.MEDIUM -> true
        LayoutType.EXPANDED -> true
    }

    val hazeState = rememberHazeState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentSearchQuery by taskViewModel.searchQuery.collectAsState()

//    var isRefreshing by remember { mutableStateOf(false) }

//    LaunchedEffect(Unit) {
//        isRefreshing = true
//        delay(3000)
//        isRefreshing = false
//    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) {
            todoViewModel.clearAllSelections()
        }
    }

    val undoActionLabel = stringResource(R.string.undo)
    val taskTextSnackbar = stringResource(R.string.task_text)
    val deletedTextSnackbar = stringResource(R.string.deleted_text)

    LaunchedEffect(Unit) {
        taskViewModel.snackbarEvent.collectLatest { event ->
            when (event) {
                is SnackbarEvent.ShowUndoDeleteSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "$taskTextSnackbar \"${event.taskItem.task}\" $deletedTextSnackbar",
                        actionLabel = undoActionLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        taskViewModel.undoRemoveItem()
                    } else {
                        taskViewModel.confirmRemoveItem()
                    }
                }
            }
        }
    }

    fun resetBottomSheetState() {
        editingTaskId = null
        textState = ""
        descriptionState = ""
        currentPriority = Priority.LOW
        selectedDueDateMillis = null
        selectedDueTimeHour = null
        selectedDueTimeMinute = null
        currentSteps.clear()
    }

    val showDummyProfile by devSettingsViewModel.showDummyProfileState.collectAsState()
    val isDeveloperModeEnabled by devSettingsViewModel.devModeToggleState.collectAsState()

    LaunchedEffect(drawerState.isOpen) {
        todoViewModel.drawerOpenFlow.emit(drawerState.isOpen)
    }

    ModalNavigationDrawer(
        drawerContent = {
            TodoListContent(
                viewModel = todoViewModel,
                onDrawerItemClicked = { _ ->
                    scope.launch { drawerState.close() }
                },
            )
        }, drawerState = drawerState
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    XenonSnackbar(
                        snackbarData = snackbarData,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            },
            bottomBar = {

                FloatingToolbarContent(
                    hazeState = hazeState,
                    onSearchQueryChanged = { newQuery ->
                        taskViewModel.setSearchQuery(newQuery)
                    },
                    currentSearchQuery = currentSearchQuery,
                    lazyListState = lazyListState,
                    allowToolbarScrollBehavior = !isAppBarCollapsible,
                    selectedNoteIds = emptyList(),
                    onClearSelection = { },
                    isAddModeActive = false,
                    onAddModeToggle = {
                        resetBottomSheetState()
                        showBottomSheet = true
                    },
                    isSearchActive = isSearchActive,
                    onIsSearchActiveChange = { isSearchActive = it },
                    defaultContent = { iconsAlphaDuration, showActionIconsExceptSearch ->
                        Row {
                            val iconAlphaTarget = if (isSearchActive) 0f else 1f

                            val sortIconAlpha by animateFloatAsState(
                                targetValue = iconAlphaTarget, animationSpec = tween(
                                    durationMillis = iconsAlphaDuration,
                                    delayMillis = if (isSearchActive) 0 else 0
                                ), label = "SortIconAlpha"
                            )
                            IconButton(
                                onClick = { showSortDialog = true },
                                modifier = Modifier.alpha(sortIconAlpha),
                                enabled = !isSearchActive && showActionIconsExceptSearch
                            ) {
                                Icon(
                                    Icons.Filled.SortByAlpha,
                                    contentDescription = stringResource(R.string.sort_tasks_description),
                                    tint = colorScheme.onSurface
                                )
                            }

                            val filterIconAlpha by animateFloatAsState(
                                targetValue = iconAlphaTarget, animationSpec = tween(
                                    durationMillis = iconsAlphaDuration,
                                    delayMillis = if (isSearchActive) 100 else 0
                                ), label = "FilterIconAlpha"
                            )
                            IconButton(
                                onClick = { showFilterDialog = true },
                                modifier = Modifier.alpha(filterIconAlpha),
                                enabled = !isSearchActive && showActionIconsExceptSearch
                            ) {
                                Icon(
                                    Icons.Filled.FilterAlt,
                                    contentDescription = stringResource(R.string.filter_tasks_description),
                                    tint = colorScheme.onSurface
                                )
                            }

                            val settingsIconAlpha by animateFloatAsState(
                                targetValue = iconAlphaTarget, animationSpec = tween(
                                    durationMillis = iconsAlphaDuration,
                                    delayMillis = if (isSearchActive) 200 else 0
                                ), label = "SettingsIconAlpha"
                            )
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.alpha(settingsIconAlpha),
                                enabled = !isSearchActive && showActionIconsExceptSearch
                            ) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = stringResource(R.string.settings),
                                    tint = colorScheme.onSurface
                                )
                            }
                        }
                    },
                )
            },
        ) { scaffoldPadding ->
            ActivityScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding()
                    .hazeSource(hazeState)
                    .onSizeChanged { newSize ->
                    },
                titleText = stringResource(id = R.string.app_name),

                expandable = isAppBarCollapsible,

                navigationIconStartPadding = MediumPadding,
                navigationIconPadding = if (isDeveloperModeEnabled && showDummyProfile) SmallPadding else MediumPadding,
                navigationIconSpacing = MediumSpacing,

                navigationIcon = {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_navigation_menu),
                        modifier = Modifier.size(24.dp)
                    )
                },

                onNavigationIconClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                },
                hasNavigationIconExtraContent = isDeveloperModeEnabled && showDummyProfile,

                navigationIconExtraContent = {
                    if (isDeveloperModeEnabled && showDummyProfile) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            GoogleProfilBorder(
                                modifier = Modifier.size(32.dp),
                            )
                            Image(
                                painter = painterResource(id = R.mipmap.default_icon),
                                contentDescription = stringResource(R.string.open_navigation_menu),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                },

                actions = {},

                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = ExtraLargeSpacing)
                    ) {
                        if (todoItemsWithHeaders.isEmpty() && currentSearchQuery.isBlank()) {
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
                        } else if (todoItemsWithHeaders.isEmpty() && currentSearchQuery.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_search_results),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        } else {
                            val reorderableLazyListState =
                                rememberReorderableLazyListState(lazyListState) { from, to ->
                                    taskViewModel.swapDisplayOrder(from.index, to.index)
                                }
                            var draggedItem: TaskItem? by remember { mutableStateOf(null) }

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    top = ExtraLargePadding,
                                    bottom = scaffoldPadding.calculateBottomPadding() + MediumPadding,
                                )
                            ) {
                                itemsIndexed(
                                    items = todoItemsWithHeaders,
                                    key = { _, item -> if (item is TaskItem) item.id else item.hashCode() }) { index, item ->
                                    when (item) {
                                        is String -> {
                                            Text(
                                                text = item,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontStyle = FontStyle.Italic
                                                ),
                                                fontWeight = FontWeight.Thin,
                                                textAlign = TextAlign.Start,
                                                fontFamily = QuicksandTitleVariable,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        top = if (index == 0) 0.dp else LargestPadding,
                                                        bottom = SmallPadding,
                                                        start = SmallPadding,
                                                        end = LargestPadding
                                                    )
                                            )
                                        }

                                        is TaskItem -> {
                                            ReorderableItem(
                                                reorderableLazyListState,
                                                item.id,
                                                enabled = draggedItem?.currentHeader == item.currentHeader
                                            ) { isDragging ->
                                                TaskItemCell(
                                                    item = item,
                                                    onToggleCompleted = {
                                                        taskViewModel.toggleCompleted(item.id)
                                                    },
                                                    onDeleteItem = {
                                                        taskViewModel.prepareRemoveItem(item.id)
                                                    },
                                                    onEditItem = { updatedItem ->
                                                        taskViewModel.updateItem(updatedItem)
                                                    },
                                                    modifier = Modifier
                                                        .draggableHandle(
                                                            enabled = true,
                                                            onDragStarted = {
                                                                draggedItem = item
                                                            },
                                                            onDragStopped = {
                                                                draggedItem = null
                                                                taskViewModel.saveAllTasks()
                                                            },
                                                            dragGestureDetector = DragGestureDetector.LongPress
                                                        )
                                                        .zIndex(if (isDragging) 4F else 0F)
                                                )
                                            }
                                            val isLastItemInListOrNextIsHeader =
                                                index == todoItemsWithHeaders.lastIndex || (index + 1 < todoItemsWithHeaders.size && todoItemsWithHeaders[index + 1] is String)

                                            if (!isLastItemInListOrNextIsHeader) {
                                                Spacer(
                                                    modifier = Modifier.Companion.height(
                                                        MediumPadding
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
//                        if (isRefreshing) {
//                            androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class
//                            Box(
//                                contentAlignment = Alignment.TopCenter,
//                                modifier = Modifier
//                                    .padding(top = LargestPadding)
//                                    .fillMaxWidth()
//                            ) {
//
//                                LoadingIndicator(   )
//                            }
//                        }
//                    }
                })

            if (showBottomSheet) {
                Box(
                    modifier = Modifier.fillMaxSize()
                    // .hazeEffect(hazeState)
                ) {
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
                            currentSteps = currentSteps.toList(),
                            onStepAdded = { stepText ->
                                if (editingTaskId != null) {
                                    taskViewModel.addStepToTask(editingTaskId!!, stepText)
                                    (taskViewModel.taskItems.find { it is TaskItem && it.id == editingTaskId } as? TaskItem)?.let { task ->
                                        currentSteps.clear()
                                        currentSteps.addAll(task.steps)
                                    }
                                } else {
                                    currentSteps.add(
                                        TaskStep(
                                            id = UUID.randomUUID().toString(),
                                            text = stepText,
                                            displayOrder = currentSteps.size
                                        )
                                    )
                                }
                            },
                            onStepToggled = { stepId ->
                                if (editingTaskId != null) {
                                    taskViewModel.toggleStepCompletion(editingTaskId!!, stepId)
                                    (taskViewModel.taskItems.find { it is TaskItem && it.id == editingTaskId } as? TaskItem)?.let { task ->
                                        currentSteps.clear()
                                        currentSteps.addAll(task.steps)
                                    }
                                } else {
                                    val stepIndex = currentSteps.indexOfFirst { it.id == stepId }
                                    if (stepIndex != -1) {
                                        val step = currentSteps[stepIndex]
                                        currentSteps[stepIndex] =
                                            step.copy(isCompleted = !step.isCompleted)
                                    }
                                }
                            },
                            onStepTextUpdated = { stepId, newText ->
                                if (editingTaskId != null) {
                                    val stepToUpdate =
                                        currentSteps.find { it.id == stepId }?.copy(text = newText)
                                    if (stepToUpdate != null) {
                                        taskViewModel.updateStepInTask(
                                            editingTaskId!!, stepToUpdate
                                        )
                                        (taskViewModel.taskItems.find { it is TaskItem && it.id == editingTaskId } as? TaskItem)?.let { task ->
                                            currentSteps.clear()
                                            currentSteps.addAll(task.steps)
                                        }
                                    }
                                } else {
                                    val stepIndex = currentSteps.indexOfFirst { it.id == stepId }
                                    if (stepIndex != -1) {
                                        currentSteps[stepIndex] =
                                            currentSteps[stepIndex].copy(text = newText)
                                    }
                                }
                            },
                            onStepRemoved = { stepId ->
                                if (editingTaskId != null) {
                                    taskViewModel.removeStepFromTask(editingTaskId!!, stepId)
                                    (taskViewModel.taskItems.find { it is TaskItem && it.id == editingTaskId } as? TaskItem)?.let { task ->
                                        currentSteps.clear()
                                        currentSteps.addAll(task.steps)
                                    }
                                } else {
                                    currentSteps.removeAll { it.id == stepId }
                                }
                            },
                            onSaveTask = { newDateMillis, newHour, newMinute ->
                                if (textState.isNotBlank()) {
                                    if (editingTaskId != null) {
                                        val updatedTask = TaskItem(
                                            id = editingTaskId!!,
                                            task = textState,
                                            description = descriptionState.takeIf { it.isNotBlank() },
                                            priority = currentPriority,
                                            dueDateMillis = newDateMillis,
                                            dueTimeHour = newHour,
                                            dueTimeMinute = newMinute,
                                            steps = currentSteps.toList(),
                                            listId = taskViewModel.taskItems.filterIsInstance<TaskItem>()
                                                .find { it.id == editingTaskId }?.listId
                                                ?: taskViewModel.currentSelectedListId
                                                ?: TaskViewModel.Companion.DEFAULT_LIST_ID,
                                            isCompleted = taskViewModel.taskItems.filterIsInstance<TaskItem>()
                                                .find { it.id == editingTaskId }?.isCompleted
                                                ?: false,
                                            creationTimestamp = taskViewModel.taskItems.filterIsInstance<TaskItem>()
                                                .find { it.id == editingTaskId }?.creationTimestamp
                                                ?: System.currentTimeMillis(),
                                            displayOrder = taskViewModel.taskItems.filterIsInstance<TaskItem>()
                                                .find { it.id == editingTaskId }?.displayOrder ?: 0
                                        )
                                        taskViewModel.updateItem(updatedTask)
                                    } else {
                                        taskViewModel.addItem(
                                            task = textState,
                                            description = descriptionState.takeIf { it.isNotBlank() },
                                            priority = currentPriority,
                                            dueDateMillis = newDateMillis,
                                            dueTimeHour = newHour,
                                            dueTimeMinute = newMinute,
                                            steps = currentSteps.toList()
                                        )
                                    }
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
            if (showSortDialog) {
                Box(
                    modifier = Modifier.fillMaxSize()
                    // .hazeEffect(hazeState)
                ) {
                    DialogTaskItemSorting(
                        currentSortOption = taskViewModel.currentSortOption,
                        currentSortOrder = taskViewModel.currentSortOrder,
                        onDismissRequest = { showSortDialog = false },
                        onApplySort = { newOption, newOrder ->
                            taskViewModel.setSortCriteria(newOption, newOrder)
                        })
                }
            }

            if (showFilterDialog) {
                Box(
                    modifier = Modifier.fillMaxSize()
                    // .hazeEffect(hazeState)
                ) {
                    DialogTaskItemFiltering(
                        initialFilterStates = taskViewModel.filterStates.toMap(),
                        onDismissRequest = { showFilterDialog = false },
                        onApplyFilters = { newStates ->
                            taskViewModel.updateMultipleFilterStates(newStates)
                        },
                        onResetFilters = {
                            taskViewModel.resetAllFilters()
                        })
                }
            }
        }
    }
}
