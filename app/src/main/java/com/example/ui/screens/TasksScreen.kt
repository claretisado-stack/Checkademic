package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicTask
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatusFilter
import com.example.ui.components.EmptyPlannerBanner
import com.example.ui.components.TaskCard
import com.example.ui.components.subjectColor
import com.example.ui.theme.CrimsonOverdue
import com.example.ui.theme.SageTeal
import com.example.ui.viewmodel.CheckademicUiState
import kotlin.math.roundToInt

@Composable
fun TasksScreen(
    uiState: CheckademicUiState,
    onStatusFilterChange: (TaskStatusFilter) -> Unit,
    onSubjectFilterChange: (Long?) -> Unit,
    onPriorityFilterChange: (TaskPriority?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearAllFilters: () -> Unit,
    onToggleTaskCompletion: (AcademicTask) -> Unit,
    onTaskClick: (AcademicTask) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCompletedSection by rememberSaveable { mutableStateOf(true) }
    val filter = uiState.filterState
    val stats = uiState.stats
    val hasActiveSecondaryFilters = filter.selectedSubjectId != null ||
        filter.selectedPriority != null ||
        filter.searchQuery.isNotBlank() ||
        filter.statusFilter != TaskStatusFilter.ALL

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Completion Summary Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due Date Tracker",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Filter by status, subject, or priority",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "${stats.completionPercentage.roundToInt()}% Done",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Search field
        item {
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search homework, quizzes, subjects...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tasks_search_input")
            )
        }

        // Status Tabs (All, Upcoming, Overdue, Completed)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatusFilter.entries.forEach { status ->
                    val count = when (status) {
                        TaskStatusFilter.ALL -> stats.totalTasks
                        TaskStatusFilter.UPCOMING -> stats.upcomingTasks
                        TaskStatusFilter.OVERDUE -> stats.overdueTasks
                        TaskStatusFilter.COMPLETED -> stats.completedTasks
                    }
                    val selected = filter.statusFilter == status
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusFilterChange(status) },
                        label = { Text("${status.label} ($count)") },
                        colors = if (status == TaskStatusFilter.OVERDUE) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = CrimsonOverdue
                            )
                        } else {
                            FilterChipDefaults.filterChipColors()
                        },
                        modifier = Modifier.testTag("filter_status_${status.name.lowercase()}")
                    )
                }
            }
        }

        // Priority & Subject Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskPriority.entries.forEach { prio ->
                    FilterChip(
                        selected = filter.selectedPriority == prio,
                        onClick = { onPriorityFilterChange(prio) },
                        label = { Text("${prio.label} Priority") },
                        modifier = Modifier.testTag("filter_priority_${prio.name.lowercase()}")
                    )
                }
                uiState.subjects.forEach { subject ->
                    val color = subjectColor(subject.colorIndex)
                    FilterChip(
                        selected = filter.selectedSubjectId == subject.id,
                        onClick = { onSubjectFilterChange(subject.id) },
                        label = { Text(subject.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    )
                }
                if (hasActiveSecondaryFilters) {
                    TextButton(onClick = onClearAllFilters) {
                        Text("Reset")
                    }
                }
            }
        }

        // Empty state handling
        if (uiState.allTasks.isEmpty()) {
            item {
                EmptyPlannerBanner(
                    title = "No assignments added yet",
                    subtitle = "Tap below to record your homework, projects, quizzes, or upcoming exams with due dates and priority levels.",
                    ctaLabel = "Create First Task",
                    onCtaClick = onAddTaskClick
                )
            }
        } else if (uiState.filteredPendingTasks.isEmpty() && uiState.filteredCompletedTasks.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No matching tasks",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try adjusting your search or clearing the active filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onClearAllFilters) {
                            Text("Clear Filters")
                        }
                    }
                }
            }
        } else {
            // Pending & Overdue Section
            if (uiState.filteredPendingTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (filter.statusFilter == TaskStatusFilter.OVERDUE) {
                                "Overdue Work (${uiState.filteredPendingTasks.size})"
                            } else {
                                "Pending & Upcoming (${uiState.filteredPendingTasks.size})"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                items(uiState.filteredPendingTasks, key = { "pending_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleTaskCompletion(task) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Separated Completed-Task Checklist Section
            if (uiState.filteredCompletedTasks.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCompletedSection = !showCompletedSection }
                            .testTag("completed_checklist_header")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Completed Checklist (${uiState.filteredCompletedTasks.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SageTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (showCompletedSection) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (showCompletedSection) "Collapse completed" else "Expand completed",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(
                    items = if (showCompletedSection) uiState.filteredCompletedTasks else emptyList(),
                    key = { "completed_${it.id}" }
                ) { task ->
                    AnimatedVisibility(visible = showCompletedSection) {
                        TaskCard(
                            task = task,
                            onToggleComplete = { onToggleTaskCompletion(task) },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
