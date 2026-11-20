package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.AcademicTask
import com.example.data.model.TaskStatusFilter
import com.example.notifications.DeadlineNotificationHelper
import com.example.ui.components.TaskDetailSheet
import com.example.ui.components.TaskEditorSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.SubjectsAndSettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.viewmodel.CheckademicViewModel
import com.example.util.DateTimeUtils
import kotlinx.coroutines.launch

private enum class TopDestination(
    val route: String,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    TASKS("tasks", R.string.nav_tasks, Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn, "nav_tasks"),
    PLANNER("planner", R.string.nav_planner, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_planner"),
    SUBJECTS("subjects", R.string.nav_subjects, Icons.Filled.Book, Icons.Outlined.Book, "nav_subjects"),
}

@Composable
fun CheckademicRoot(
    initialTaskIdFromNotification: Long = -1L,
    viewModel: CheckademicViewModel = viewModel(factory = CheckademicViewModel.Factory),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(DeadlineNotificationHelper.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        scope.launch {
            snackbarHostState.showSnackbar(
                if (granted) "Deadline reminders enabled!" else "Notifications are turned off."
            )
        }
    }

    val requestNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermission = true
        }
    }

    var showEditorSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<AcademicTask?>(null) }
    var editorInitialDueDateMillis by remember {
        mutableLongStateOf(DateTimeUtils.startOfDay(System.currentTimeMillis()))
    }
    var detailTask by remember { mutableStateOf<AcademicTask?>(null) }

    // Open task details if launched from a reminder notification
    LaunchedEffect(initialTaskIdFromNotification, uiState.allTasks) {
        if (initialTaskIdFromNotification > 0L) {
            uiState.allTasks.firstOrNull { it.id == initialTaskIdFromNotification }?.let { matched ->
                detailTask = matched
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TopDestination.DASHBOARD.route

    val navigateToTab: (TopDestination) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val handleToggleComplete: (AcademicTask) -> Unit = { task ->
        viewModel.toggleTaskCompletion(task)
        scope.launch {
            val message = if (task.isCompleted) {
                "\"${task.title}\" moved back to pending"
            } else {
                "Completed \"${task.title}\"!"
            }
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    val handleDeleteTask: (AcademicTask) -> Unit = { task ->
        viewModel.deleteTask(task.id)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Deleted \"${task.title}\"",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreTask(task)
            }
        }
    }

    val openNewTaskEditor: (Long) -> Unit = { targetDateMillis ->
        editingTask = null
        editorInitialDueDateMillis = targetDateMillis
        showEditorSheet = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val defaultDue = if (currentRoute == TopDestination.PLANNER.route) {
                            uiState.filterState.selectedDateMillis
                        } else {
                            DateTimeUtils.addDays(DateTimeUtils.startOfDay(System.currentTimeMillis()), 1)
                        }
                        openNewTaskEditor(defaultDue)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cd_add_task)
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        TopDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelResId)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        TopDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelResId)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = TopDestination.DASHBOARD.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(TopDestination.DASHBOARD.route) {
                            DashboardScreen(
                                uiState = uiState,
                                needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !hasNotificationPermission,
                                onRequestNotificationPermission = requestNotificationPermission,
                                onNavigateToTasksWithFilter = { filter ->
                                    viewModel.setStatusFilter(filter)
                                    navigateToTab(TopDestination.TASKS)
                                },
                                onToggleTaskCompletion = handleToggleComplete,
                                onTaskClick = { detailTask = it },
                                onAddTaskClick = {
                                    openNewTaskEditor(
                                        DateTimeUtils.addDays(
                                            DateTimeUtils.startOfDay(System.currentTimeMillis()),
                                            1
                                        )
                                    )
                                }
                            )
                        }

                        composable(TopDestination.TASKS.route) {
                            TasksScreen(
                                uiState = uiState,
                                onStatusFilterChange = viewModel::setStatusFilter,
                                onSubjectFilterChange = viewModel::setSubjectFilter,
                                onPriorityFilterChange = viewModel::setPriorityFilter,
                                onSearchQueryChange = viewModel::setSearchQuery,
                                onClearAllFilters = viewModel::clearAllFilters,
                                onToggleTaskCompletion = handleToggleComplete,
                                onTaskClick = { detailTask = it },
                                onAddTaskClick = {
                                    openNewTaskEditor(
                                        DateTimeUtils.addDays(
                                            DateTimeUtils.startOfDay(System.currentTimeMillis()),
                                            1
                                        )
                                    )
                                }
                            )
                        }

                        composable(TopDestination.PLANNER.route) {
                            PlannerScreen(
                                uiState = uiState,
                                onTogglePlannerMode = viewModel::setWeeklyPlannerMode,
                                onSelectDate = viewModel::selectPlannerDate,
                                onShiftWeek = viewModel::shiftPlannerWeek,
                                onShiftMonth = viewModel::shiftPlannerMonth,
                                onToggleTaskCompletion = handleToggleComplete,
                                onTaskClick = { detailTask = it },
                                onAddTaskForDate = { selectedDay -> openNewTaskEditor(selectedDay) }
                            )
                        }

                        composable(TopDestination.SUBJECTS.route) {
                            SubjectsAndSettingsScreen(
                                uiState = uiState,
                                hasNotificationPermission = hasNotificationPermission,
                                onRequestNotificationPermission = requestNotificationPermission,
                                onSaveSubject = viewModel::saveSubject,
                                onDeleteSubject = viewModel::deleteSubject,
                                onFilterTasksBySubject = { subjectId ->
                                    viewModel.setStatusFilter(TaskStatusFilter.ALL)
                                    viewModel.setSubjectFilter(subjectId)
                                    navigateToTab(TopDestination.TASKS)
                                },
                                onToggleReminders = viewModel::setRemindersEnabled,
                                onSelectDefaultLeadTime = viewModel::setDefaultLeadTime,
                                onSendTestReminder = {
                                    val candidate = uiState.stats.dueSoonTasks.firstOrNull()
                                        ?: uiState.allTasks.firstOrNull { !it.isCompleted }
                                    DeadlineNotificationHelper.showSampleTestReminder(context, candidate)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Sample reminder notification sent!")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    detailTask?.let { currentDetail ->
        TaskDetailSheet(
            task = currentDetail,
            onDismiss = { detailTask = null },
            onToggleComplete = { handleToggleComplete(currentDetail) },
            onEdit = {
                detailTask = null
                editingTask = currentDetail
                editorInitialDueDateMillis = currentDetail.dueDateMillis
                showEditorSheet = true
            },
            onDelete = { handleDeleteTask(currentDetail) }
        )
    }

    if (showEditorSheet) {
        TaskEditorSheet(
            editingTask = editingTask,
            initialDueDateMillis = editorInitialDueDateMillis,
            defaultReminderMinutes = uiState.reminderSettings.defaultLeadMinutes,
            subjects = uiState.subjects,
            onDismiss = {
                showEditorSheet = false
                editingTask = null
            },
            onSave = { id, title, subject, colorIdx, cat, prio, desc, assigned, due, lead, completed, completedAt ->
                viewModel.saveTask(
                    existingId = id,
                    title = title,
                    subjectName = subject,
                    subjectColorIndex = colorIdx,
                    category = cat,
                    priority = prio,
                    description = desc,
                    assignedDateMillis = assigned,
                    dueDateMillis = due,
                    reminderLeadMinutes = lead,
                    isCompleted = completed,
                    completedAtMillis = completedAt
                )
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (id == 0L) "Added \"$title\"" else "Updated \"$title\""
                    )
                }
            },
            onDelete = { id ->
                editingTask?.let { handleDeleteTask(it) } ?: viewModel.deleteTask(id)
            }
        )
    }
}
