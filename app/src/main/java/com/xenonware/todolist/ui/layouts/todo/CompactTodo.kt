package com.xenonware.todolist.ui.layouts.todo

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.Identity
import com.xenon.mylibrary.ActivityScreen
import com.xenon.mylibrary.res.FloatingToolbarContent
import com.xenon.mylibrary.res.GoogleProfilBorder
import com.xenon.mylibrary.res.GoogleProfilePicture
import com.xenon.mylibrary.res.SpannedModeFAB
import com.xenon.mylibrary.res.XenonSnackbar
import com.xenon.mylibrary.theme.DeviceConfigProvider
import com.xenon.mylibrary.theme.LocalDeviceConfig
import com.xenon.mylibrary.theme.QuicksandTitleVariable
import com.xenon.mylibrary.values.ExtraLargePadding
import com.xenon.mylibrary.values.ExtraLargeSpacing
import com.xenon.mylibrary.values.LargePadding
import com.xenon.mylibrary.values.LargestPadding
import com.xenon.mylibrary.values.MediumPadding
import com.xenon.mylibrary.values.MediumSpacing
import com.xenon.mylibrary.values.SmallPadding
import com.xenonware.todolist.R
import com.xenonware.todolist.data.SharedPreferenceManager
import com.xenonware.todolist.presentation.sign_in.GoogleAuthUiClient
import com.xenonware.todolist.presentation.sign_in.SignInViewModel
import com.xenonware.todolist.ui.res.DialogTaskItemFiltering
import com.xenonware.todolist.ui.res.DialogTaskItemSorting
import com.xenonware.todolist.ui.res.TaskItemCell
import com.xenonware.todolist.ui.res.TaskSheet
import com.xenonware.todolist.ui.res.TodoListContent
import com.xenonware.todolist.ui.theme.LocalIsDarkTheme
import com.xenonware.todolist.ui.theme.extendedMaterialColorScheme
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
import java.util.Calendar
import java.util.Locale

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CompactTodo(
    viewModel: TaskViewModel = viewModel(),
    layoutType: LayoutType,
    isLandscape: Boolean,
    onOpenSettings: () -> Unit,
    appSize: IntSize,
) {

    DeviceConfigProvider(appSize = appSize) {
        val deviceConfig = LocalDeviceConfig.current
        val application = LocalContext.current.applicationContext as Application
        val todoViewModel: TodoViewModel = viewModel(
            factory = TodoViewModelFactory(application, viewModel)
        )
        val context = LocalContext.current
        val sharedPreferenceManager = remember { SharedPreferenceManager(context) }

        var showDatePicker by rememberSaveable { mutableStateOf(false) }
        var showTimePicker by rememberSaveable { mutableStateOf(false) }

        val showTaskSheet by viewModel.showTaskSheet.collectAsStateWithLifecycle()
        val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
        var editingTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
        var textState by rememberSaveable { mutableStateOf("") }
        var saveTrigger by remember { mutableStateOf(false) }
        var descriptionState by rememberSaveable { mutableStateOf("") }
        var currentPriority by rememberSaveable { mutableStateOf(Priority.LOW) }
        var selectedDueDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
        var selectedDueTimeHour by rememberSaveable { mutableStateOf<Int?>(null) }
        var selectedDueTimeMinute by rememberSaveable { mutableStateOf<Int?>(null) }
        val currentSteps = remember { mutableStateListOf<TaskStep>() }
        var isSearchActive by rememberSaveable { mutableStateOf(false) }

        val isDarkTheme = LocalIsDarkTheme.current
        val isBlackedOut by produceState(
            initialValue = sharedPreferenceManager.blackedOutModeEnabled && isDarkTheme
        ) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "blacked_out_mode_enabled") {
                    value = sharedPreferenceManager.blackedOutModeEnabled
                }
            }
            sharedPreferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(
                listener
            )
            awaitDispose {
                sharedPreferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                    listener
                )
            }
        }


        val selectedListId by todoViewModel.selectedDrawerItemId

        LaunchedEffect(selectedListId) {
            viewModel.currentSelectedListId = selectedListId
        }
        
        LaunchedEffect(showTaskSheet, editingTask) {
             if (showTaskSheet) {
                 textState = editingTask?.task ?: ""
                 selectedDueDateMillis = editingTask?.dueDateMillis
                 selectedDueTimeHour = editingTask?.dueTimeHour
                 selectedDueTimeMinute = editingTask?.dueTimeMinute
             }
        }

        val todoItemsWithHeaders = viewModel.taskItems

        val configuration = LocalConfiguration.current
        val appHeight = configuration.screenHeightDp.dp
        val isAppBarExpandable = when (layoutType) {
            LayoutType.COVER -> false
            LayoutType.SMALL -> false
            LayoutType.COMPACT -> !isLandscape && appHeight >= 460.dp
            LayoutType.MEDIUM -> true
            LayoutType.EXPANDED -> true
        }

        val hazeState = rememberHazeState()

        var showSortDialog by remember { mutableStateOf(false) }
        var showFilterDialog by remember { mutableStateOf(false) }


        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val currentSearchQuery by viewModel.searchQuery.collectAsState()


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
            viewModel.snackbarEvent.collectLatest { event ->
                when (event) {
                    is SnackbarEvent.ShowUndoDeleteSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
                            message = "$taskTextSnackbar \"${event.taskItem.task}\" $deletedTextSnackbar",
                            actionLabel = undoActionLabel,
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoRemoveItem()
                        } else {
                            viewModel.confirmRemoveItem()
                        }
                    }
                }
            }
        }

        val googleAuthUiClient = remember {
            GoogleAuthUiClient(
                context = context.applicationContext,
                oneTapClient = Identity.getSignInClient(context.applicationContext)
            )
        }
        val signInViewModel: SignInViewModel = viewModel()

        LaunchedEffect(drawerState.isOpen) {
            todoViewModel.drawerOpenFlow.emit(drawerState.isOpen)
        }

        ModalNavigationDrawer(
            drawerContent = {
                TodoListContent(
                    viewModel = todoViewModel,
                    signInViewModel = signInViewModel,
                    googleAuthUiClient = googleAuthUiClient,
                    onDrawerItemClicked = { _ ->
                        scope.launch { drawerState.close() }
                    },
                )
            }, drawerState = drawerState
        ) {
            Scaffold(snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    XenonSnackbar(
                        snackbarData = snackbarData,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }, bottomBar = {
                val bottomPaddingNavigationBar =
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val imePaddingValues = WindowInsets.ime.asPaddingValues()
                val imeHeight = imePaddingValues.calculateBottomPadding()

                val targetBottomPadding =
                    remember(imeHeight, bottomPaddingNavigationBar, imePaddingValues) {
                        val calculatedPadding = if (imeHeight > bottomPaddingNavigationBar) {
                            imeHeight + LargePadding
                        } else {
                            max(
                                bottomPaddingNavigationBar,
                                imePaddingValues.calculateTopPadding()
                            ) + LargePadding
                        }
                        max(calculatedPadding, 0.dp)
                    }

                val animatedBottomPadding by animateDpAsState(
                    targetValue = targetBottomPadding, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ), label = "bottomPaddingAnimation"
                )

                FloatingToolbarContent(
                    hazeState = hazeState,
                    onSearchQueryChanged = { newQuery ->
                        viewModel.setSearchQuery(newQuery)
                    },
                    currentSearchQuery = currentSearchQuery,
                    lazyListState = lazyListState,
                    allowToolbarScrollBehavior = !isAppBarExpandable,
                    isSelectedColor = extendedMaterialColorScheme.inverseErrorContainer,
                    selectedNoteIds = emptyList(),
                    onClearSelection = { },
                    isAddModeActive = false,
                    onAddModeToggle = {
                        viewModel.showTaskSheetForNewTask()
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
                                    Icons.Rounded.SortByAlpha,
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
                                    Icons.Rounded.FilterAlt,
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
                                    Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.settings),
                                    tint = colorScheme.onSurface
                                )
                            }
                        }
                    },
                    contentOverride = if (showTaskSheet) {
                        @Composable {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Animated colors
                                val dateContainerColor by animateColorAsState(
                                    targetValue = if (selectedDueDateMillis != null) colorScheme.tertiary else colorScheme.surfaceBright,
                                    label = "dateContainer"
                                )
                                val dateContentColor by animateColorAsState(
                                    targetValue = if (selectedDueDateMillis != null) colorScheme.onTertiary else colorScheme.onSurface,
                                    label = "dateContent"
                                )

                                val timeContainerColor by animateColorAsState(
                                    targetValue = if (selectedDueTimeHour != null && selectedDueTimeMinute != null) colorScheme.tertiary else colorScheme.surfaceBright,
                                    label = "timeContainer"
                                )
                                val timeContentColor by animateColorAsState(
                                    targetValue = if (selectedDueTimeHour != null && selectedDueTimeMinute != null) colorScheme.onTertiary else colorScheme.onSurface,
                                    label = "timeContent"
                                )

                                val dateShape by animateDpAsState(
                                    targetValue = if (selectedDueDateMillis != null) 28.dp else 8.dp,
                                    label = "dateCorner"
                                )
                                val timeShape by animateDpAsState(
                                    targetValue = if (selectedDueTimeHour != null && selectedDueTimeMinute != null) 28.dp else 8.dp,
                                    label = "timeCorner"
                                )

                                // Time Box – now clickable
                                Box(
                                    modifier = Modifier
                                        .width(95.dp)
                                        .height(56.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 28.dp,
                                                bottomStart = 28.dp,
                                                topEnd = timeShape,
                                                bottomEnd = timeShape
                                            )
                                        )
                                        .background(timeContainerColor)
                                        .combinedClickable(
                                            onClick = { showTimePicker = true },
                                            onLongClick = {
                                                selectedDueTimeHour = null
                                                selectedDueTimeMinute = null
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val timeText =
                                        if (selectedDueTimeHour != null && selectedDueTimeMinute != null) {
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, selectedDueTimeHour!!)
                                                set(Calendar.MINUTE, selectedDueTimeMinute!!)
                                            }
                                            DateFormat.format("HH:mm", cal).toString()
                                        } else stringResource(id = R.string.select_time)

                                    Text(
                                        text = timeText,
                                        style = typography.labelLarge,
                                        color = timeContentColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(Modifier.width(2.dp))

                                // Date Box – now clickable and triggers callback
                                Box(
                                    modifier = Modifier
                                        .width(95.dp)
                                        .height(56.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = dateShape,
                                                bottomStart = dateShape,
                                                topEnd = 28.dp,
                                                bottomEnd = 28.dp
                                            )
                                        )
                                        .background(dateContainerColor)
                                        .combinedClickable(
                                            onClick = { showDatePicker = true },
                                            onLongClick = { selectedDueDateMillis = null }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val dateText = selectedDueDateMillis?.let { millis ->
                                        val sdf = java.text.SimpleDateFormat(
                                            "MMM dd, yy", Locale.getDefault()
                                        )
                                        sdf.format(java.util.Date(millis))
                                    } ?: stringResource(id = R.string.select_date)

                                    Text(
                                        text = dateText,
                                        style = typography.labelLarge,
                                        color = dateContentColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else null,

                    fabOverride = if (showTaskSheet) {
                        @Composable {
                            // Sync with TaskSheet title
                            val canSave = textState.isNotBlank()
                            FloatingActionButton(
                                onClick = {
                                    if (canSave) {
                                        saveTrigger = true
                                    }
                                },
                                containerColor = colorScheme.primary,
                                contentColor = if (canSave) colorScheme.onPrimary else colorScheme.onPrimary.copy(
                                    alpha = 0.38f
                                )
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = "Save task")
                            }
                        }
                    } else null,
                    isSpannedMode = deviceConfig.isSpannedMode,
                    fabOnLeftInSpannedMode = deviceConfig.fabOnLeft,
                    spannedModeHingeGap = deviceConfig.hingeGapDp,
                    spannedModeFab = {
                        SpannedModeFAB(
                            hazeState = hazeState,
                            onClick = deviceConfig.toggleFabSide,
                            modifier = Modifier.padding(bottom = animatedBottomPadding),
                        )
                    })
            }) { scaffoldPadding ->
                val context = LocalContext.current
                val googleAuthUiClient = remember {
                    GoogleAuthUiClient(
                        context = context.applicationContext,
                        oneTapClient = Identity.getSignInClient(context.applicationContext)
                    )
                }
                val signInViewModel: SignInViewModel = viewModel()
                val state by signInViewModel.state.collectAsStateWithLifecycle()
                val userData = googleAuthUiClient.getSignedInUser()
                ActivityScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding()
                        .hazeSource(hazeState)
                        .onSizeChanged { _ ->
                        },
                    titleText = stringResource(id = R.string.app_name),

                    expandable = isAppBarExpandable,

                    navigationIconStartPadding = MediumPadding,
                    navigationIconPadding = if (state.isSignInSuccessful) SmallPadding else MediumPadding,
                    navigationIconSpacing = MediumSpacing,

                    navigationIcon = {
                        Icon(
                            Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.open_navigation_menu),
                            modifier = Modifier.size(24.dp)
                        )
                    },

                    onNavigationIconClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    hasNavigationIconExtraContent = state.isSignInSuccessful,

                    navigationIconExtraContent = {
                        if (state.isSignInSuccessful) {
                            Box(contentAlignment = Alignment.Center) {
                                GoogleProfilBorder(
                                    isSignedIn = true,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.5.dp
                                )

                                GoogleProfilePicture(
                                    noAccIcon = painterResource(id = R.drawable.default_icon),
                                    profilePictureUrl = userData?.profilePictureUrl,
                                    contentDescription = stringResource(R.string.profile_picture),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    },

                    actions = {},

                    content = {
                        Box(Modifier.fillMaxSize()) {
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
                                            viewModel.swapDisplayOrder(from.index, to.index)
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
                                                                viewModel.toggleCompleted(item.id)
                                                            },
                                                            onDeleteItem = {
                                                                viewModel.prepareRemoveItem(item.id)
                                                            },
                                                            modifier = Modifier
                                                                .draggableHandle(
                                                                    enabled = true,
                                                                    onDragStarted = {
                                                                        draggedItem = item
                                                                    },
                                                                    onDragStopped = {
                                                                        draggedItem = null
                                                                        viewModel.saveAllTasks()
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
                                                            modifier = Modifier.height(
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
                        }
                    })

                AnimatedVisibility(
                    visible = showTaskSheet,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BackHandler {
                        viewModel.hideTaskSheet()
                    }
                    TaskSheet(
                        onDismiss = { viewModel.hideTaskSheet() },
                        onSave = { task, desc, prio, date, hour, min, steps ->
                            viewModel.saveTask(task, desc, prio, date, hour, min, steps)
                        },
                        initialTask = editingTask?.task ?: "",
                        initialDescription = editingTask?.description,
                        initialPriority = editingTask?.priority ?: Priority.LOW,
                        initialDueDateMillis = selectedDueDateMillis,
                        initialDueTimeHour = selectedDueTimeHour,
                        initialDueTimeMinute = selectedDueTimeMinute,
                        initialSteps = editingTask?.steps ?: emptyList(),
                        isBlackThemeActive = isBlackedOut,
                        isCoverModeActive = false,
                        showDatePicker = showDatePicker,
                        showTimePicker = showTimePicker,
                        onDatePickerDismiss = { showDatePicker = false },
                        onTimePickerDismiss = { showTimePicker = false },
                        onTaskTitleChange = { textState = it },
                        saveTrigger = saveTrigger,
                        onSaveTriggerConsumed = { saveTrigger = false },
                        onDateChange = { newDate ->
                            selectedDueDateMillis = newDate
                        },
                        onTimeChange = { hour, minute ->
                            selectedDueTimeHour = hour
                            selectedDueTimeMinute = minute
                        })
                }
                if (showSortDialog) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        DialogTaskItemSorting(
                            currentSortOption = viewModel.currentSortOption,
                            currentSortOrder = viewModel.currentSortOrder,
                            onDismissRequest = { showSortDialog = false },
                            onApplySort = { newOption, newOrder ->
                                viewModel.setSortCriteria(newOption, newOrder)
                            })
                    }
                }

                if (showFilterDialog) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        DialogTaskItemFiltering(
                            initialFilterStates = viewModel.filterStates.toMap(),
                            onDismissRequest = { showFilterDialog = false },
                            onApplyFilters = { newStates ->
                                viewModel.updateMultipleFilterStates(newStates)
                            },
                            onResetFilters = {
                                viewModel.resetAllFilters()
                            })
                    }
                }
            }
        }
    }
}
