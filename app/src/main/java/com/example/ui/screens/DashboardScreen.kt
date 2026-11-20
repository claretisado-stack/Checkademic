package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicTask
import com.example.data.model.TaskStatusFilter
import com.example.ui.components.CompletionRing
import com.example.ui.components.EmptyPlannerBanner
import com.example.ui.components.TaskCard
import com.example.ui.components.subjectColor
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberTerracotta
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltInk
import com.example.ui.theme.CrimsonOverdue
import com.example.ui.theme.MintContainer
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnCobaltContainer
import com.example.ui.theme.OnMintContainer
import com.example.ui.theme.OnRoseContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.SageTeal
import com.example.ui.viewmodel.CheckademicUiState
import com.example.ui.viewmodel.SubjectProgress
import com.example.util.DateTimeUtils
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    uiState: CheckademicUiState,
    needsNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onNavigateToTasksWithFilter: (TaskStatusFilter) -> Unit,
    onToggleTaskCompletion: (AcademicTask) -> Unit,
    onTaskClick: (AcademicTask) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = uiState.stats
    val todayLabel = DateTimeUtils.formatFullDate(System.currentTimeMillis())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editorial Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = todayLabel.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Checkademic",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Stay ahead of homework, quizzes, projects, and exams.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Notification permission banner on Android 13+
        if (needsNotificationPermission) {
            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Deadline Reminders",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Get notified before assignments and exams are due.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onRequestNotificationPermission,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("enable_notifications_button")
                        ) {
                            Text("Allow")
                        }
                    }
                }
            }
        }

        // Progress Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_progress_card"),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f),
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        CompletionRing(
                            percentage = stats.completionPercentage,
                            size = 98.dp,
                            strokeWidth = 10.dp
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Academic Progress",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (stats.totalTasks == 0) {
                                    "No activities scheduled yet"
                                } else {
                                    "${stats.completedTasks} of ${stats.totalTasks} tasks completed"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { (stats.completionPercentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SageTeal,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = when {
                                    stats.totalTasks == 0 -> "Add your first task to start tracking"
                                    stats.overdueTasks > 0 -> "${stats.overdueTasks} overdue item(s) need attention"
                                    stats.pendingTasks == 0 -> "Everything is caught up!"
                                    else -> "${stats.pendingTasks} pending item(s) remaining"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (stats.overdueTasks > 0) CrimsonOverdue else SageTeal
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Summary Stat Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatTile(
                        title = "Total Tasks",
                        count = stats.totalTasks,
                        icon = Icons.Filled.Assignment,
                        accentColor = CobaltInk,
                        containerColor = CobaltContainer.copy(alpha = 0.55f),
                        contentColor = OnCobaltContainer,
                        onClick = { onNavigateToTasksWithFilter(TaskStatusFilter.ALL) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_tasks")
                    )
                    DashboardStatTile(
                        title = "Completed",
                        count = stats.completedTasks,
                        icon = Icons.Filled.CheckCircle,
                        accentColor = SageTeal,
                        containerColor = MintContainer.copy(alpha = 0.65f),
                        contentColor = OnMintContainer,
                        onClick = { onNavigateToTasksWithFilter(TaskStatusFilter.COMPLETED) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_completed_tasks")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatTile(
                        title = "Pending",
                        count = stats.pendingTasks,
                        icon = Icons.Filled.PendingActions,
                        accentColor = AmberTerracotta,
                        containerColor = AmberContainer.copy(alpha = 0.65f),
                        contentColor = OnAmberContainer,
                        onClick = { onNavigateToTasksWithFilter(TaskStatusFilter.UPCOMING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_pending_tasks")
                    )
                    DashboardStatTile(
                        title = "Overdue",
                        count = stats.overdueTasks,
                        icon = Icons.Filled.WarningAmber,
                        accentColor = CrimsonOverdue,
                        containerColor = RoseContainer.copy(alpha = 0.65f),
                        contentColor = OnRoseContainer,
                        onClick = { onNavigateToTasksWithFilter(TaskStatusFilter.OVERDUE) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_overdue_tasks")
                    )
                }
            }
        }

        // Empty state when user has no tasks yet
        if (stats.totalTasks == 0) {
            item {
                EmptyPlannerBanner(
                    title = "Ready for a productive term?",
                    subtitle = "Add your homework, projects, quizzes, and exam dates to unlock your deadline tracker, weekly planner, and progress insights.",
                    ctaLabel = "Add First Task",
                    onCtaClick = onAddTaskClick
                )
            }
        } else {
            // Overdue Spotlight
            if (stats.overdueHighlightTasks.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Overdue Attention",
                        actionLabel = "View all",
                        onActionClick = { onNavigateToTasksWithFilter(TaskStatusFilter.OVERDUE) }
                    )
                }
                items(stats.overdueHighlightTasks.take(3), key = { "overdue_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleTaskCompletion(task) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Approaching Deadlines ("Due Soon" within 48h)
            item {
                SectionHeader(
                    title = "Approaching Deadlines",
                    actionLabel = "All tasks",
                    onActionClick = { onNavigateToTasksWithFilter(TaskStatusFilter.UPCOMING) }
                )
            }
            if (stats.dueSoonTasks.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (stats.upcomingTasks > 0) {
                                "No deadlines due in the next 48 hours. You have ${stats.upcomingTasks} upcoming task(s) scheduled later."
                            } else {
                                "All upcoming deadlines are clear!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(stats.dueSoonTasks.take(4), key = { "soon_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleTaskCompletion(task) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Subject Progress Breakdown
            if (stats.subjectProgress.isNotEmpty()) {
                item {
                    Text(
                        text = "Subject Progress",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            stats.subjectProgress.forEach { progress ->
                                SubjectProgressRow(progress)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun DashboardStatTile(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onActionClick)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SubjectProgressRow(progress: SubjectProgress) {
    val tint = subjectColor(progress.subject.colorIndex)
    val pct = (progress.completionFraction * 100f).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(tint)
                )
                Text(
                    text = progress.subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${progress.completedCount}/${progress.totalCount} ($pct%)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress.completionFraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = tint,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}
