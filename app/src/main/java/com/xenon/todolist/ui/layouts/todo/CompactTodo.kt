package com.xenon.todolist.ui.layouts.todo

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.layouts.QuicksandTitleVariable
import com.xenon.todolist.ui.res.DialogTaskItemFiltering
import com.xenon.todolist.ui.res.DialogTaskItemSorting
import com.xenon.todolist.ui.res.FloatingToolbarContent
import com.xenon.todolist.ui.res.TaskItemCell
import com.xenon.todolist.ui.res.TaskItemContent
import com.xenon.todolist.ui.res.TodoListContent
import com.xenon.todolist.ui.res.XenonSnackbar
import com.xenon.todolist.ui.values.DialogPadding
import com.xenon.todolist.ui.values.ExtraLargePadding
import com.xenon.todolist.ui.values.ExtraLargeSpacing
import com.xenon.todolist.ui.values.LargestPadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.ui.values.SmallPadding
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.SnackbarEvent
import com.xenon.todolist.viewmodel.TaskViewModel
import com.xenon.todolist.viewmodel.TodoViewModel
import com.xenon.todolist.viewmodel.TodoViewModelFactory
import com.xenon.todolist.viewmodel.classes.Priority
import com.xenon.todolist.viewmodel.classes.TaskItem
import com.xenon.todolist.viewmodel.classes.TaskStep
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID


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

    var editingTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
    var textState by rememberSaveable { mutableStateOf("") }
    var descriptionState by rememberSaveable { mutableStateOf("") }
    var currentPriority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var selectedDueDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedDueTimeHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedDueTimeMinute by rememberSaveable { mutableStateOf<Int?>(null) }
    val currentSteps = remember { mutableStateListOf<TaskStep>() }


    val selectedListId by todoViewModel.selectedDrawerItemId

    LaunchedEffect(selectedListId) {
        taskViewModel.currentSelectedListId = selectedListId
    }

    val todoItemsWithHeaders = taskViewModel.taskItems

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

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentSearchQuery by taskViewModel.searchQuery.collectAsState()

    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) {
            todoViewModel.clearAllSelections()
        }
    }

    val undoActionLabel = stringResource(R.string.undo)
    LaunchedEffect(Unit) {
        taskViewModel.snackbarEvent.collectLatest { event ->
            when (event) {
                is SnackbarEvent.ShowUndoDeleteSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Task \"${event.taskItem.task}\" deleted",
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

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            TodoListContent(
                viewModel = todoViewModel, onDrawerItemClicked = { _ ->
                    scope.launch { drawerState.close() }
                })
        }) {
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
                    onShowBottomSheet = {
                        resetBottomSheetState()
                        showBottomSheet = true
                    },
                    onOpenSettings = onOpenSettings,
                    onOpenSortDialog = { showSortDialog = true },
                    onOpenFilterDialog = { showFilterDialog = true },
                    currentSearchQuery = currentSearchQuery,
                    onSearchQueryChanged = { newQuery ->
                        taskViewModel.setSearchQuery(newQuery)
                    })
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
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    top = ExtraLargePadding,
                                    bottom = scaffoldPadding.calculateBottomPadding() + MediumPadding
                                )
                            ) {
                                itemsIndexed(
                                    items = todoItemsWithHeaders,
                                    key = { _, item -> if (item is TaskItem) item.id else item.hashCode() }
                                ) { index, item ->
                                    when (item) {
                                        is String -> {
                                            Text(
                                                text = item,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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
                                            TaskItemCell(
                                                item = item,
                                                onToggleCompleted = {
                                                    taskViewModel.toggleCompleted(item.id)
                                                },
                                                onDeleteItem = {
                                                    taskViewModel.prepareRemoveItem(item.id)
                                                },
                                                onEditItem = { updatedTask ->
                                                    taskViewModel.updateItem(updatedTask)
                                                }
                                            )
                                            val isLastItemInList = index == todoItemsWithHeaders.lastIndex
                                            val nextItemIsHeader =
                                                if (!isLastItemInList && index + 1 < todoItemsWithHeaders.size) {
                                                    todoItemsWithHeaders[index + 1] is String
                                                } else false

                                            if (!isLastItemInList && !nextItemIsHeader) {
                                                Spacer(modifier = Modifier.height(MediumPadding))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )

            if (showBottomSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                    // .hazeEffect(hazeState)
                ) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState,
                        modifier = Modifier.imePadding()
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
                                            editingTaskId!!,
                                            stepToUpdate
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
                                                ?: TaskViewModel.DEFAULT_LIST_ID,
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
                    modifier = Modifier
                        .fillMaxSize()
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
                    modifier = Modifier
                        .fillMaxSize()
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
