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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.layouts.ActivityScreen
import com.xenon.todolist.ui.res.DialogTaskItemFiltering
import com.xenon.todolist.ui.res.DialogTaskItemSorting
import com.xenon.todolist.ui.res.FloatingToolbarContent
import com.xenon.todolist.ui.res.TaskItemCell
import com.xenon.todolist.ui.res.TaskItemContent
import com.xenon.todolist.ui.res.TodoListContent
import com.xenon.todolist.ui.values.DialogPadding
import com.xenon.todolist.ui.values.ExtraLargePadding
import com.xenon.todolist.ui.values.ExtraLargeSpacing
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.LayoutType
import com.xenon.todolist.viewmodel.TaskViewModel
import com.xenon.todolist.viewmodel.TodoViewModel
import com.xenon.todolist.viewmodel.TodoViewModelFactory
import com.xenon.todolist.viewmodel.classes.Priority
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
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

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }


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
                FloatingToolbarContent(
                    hazeState = hazeState,
                    onShowBottomSheet = {
                        resetBottomSheetState()
                        showBottomSheet = true
                    },
                    onOpenSettings = onOpenSettings,
                    onOpenSortDialog = { showSortDialog = true },
                    onOpenFilterDialog = { showFilterDialog = true }
                )
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

            if (showSortDialog) {
                DialogTaskItemSorting(
                    currentSortOption = taskViewModel.currentSortOption,
                    currentSortOrder = taskViewModel.currentSortOrder,
                    onDismissRequest = { showSortDialog = false },
                    onApplySort = { newOption, newOrder ->
                        taskViewModel.setSortCriteria(newOption, newOrder)
                    }
                )
            }

            if (showFilterDialog) {
                DialogTaskItemFiltering(
                    initialFilterStates = taskViewModel.filterStates.toMap(), // Pass a defensive copy
                    onDismissRequest = { showFilterDialog = false }, // Handled by XenonDialog's 'X' or scrim click
                    onApplyFilters = { newStates ->
                        taskViewModel.updateMultipleFilterStates(newStates) // Or loop and call updateFilterState
                        // showFilterDialog = false; // XenonDialog's confirm button also calls onDismissRequest
                    },
                    onResetFilters = {
                        taskViewModel.resetAllFilters()
                        // Dialog remains open for user to confirm reset (by applying) or making new changes.
                        // The internal state of DialogTaskItemFiltering is also reset.
                    }
                )
            }
        }
    }
}
