package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicTask
import com.example.ui.components.TaskCard
import com.example.ui.components.subjectColor
import com.example.ui.theme.CrimsonOverdue
import com.example.ui.theme.SageTeal
import com.example.ui.viewmodel.CheckademicUiState
import com.example.util.DateTimeUtils

private val WEEKDAY_HEADERS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun PlannerScreen(
    uiState: CheckademicUiState,
    onTogglePlannerMode: (Boolean) -> Unit,
    onSelectDate: (Long) -> Unit,
    onShiftWeek: (Int) -> Unit,
    onShiftMonth: (Int) -> Unit,
    onToggleTaskCompletion: (AcademicTask) -> Unit,
    onTaskClick: (AcademicTask) -> Unit,
    onAddTaskForDate: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filter = uiState.filterState
    val todayMillis = remember { DateTimeUtils.startOfDay(System.currentTimeMillis()) }
    val selectedDayMillis = filter.selectedDateMillis
    val weekStartMillis = remember(selectedDayMillis) { DateTimeUtils.startOfWeekMonday(selectedDayMillis) }
    val weekDays = remember(weekStartMillis) { DateTimeUtils.getWeekDaysFrom(weekStartMillis) }
    val monthGrid = remember(filter.calendarMonthAnchorMillis) {
        DateTimeUtils.getMonthGrid(filter.calendarMonthAnchorMillis)
    }

    val tasksDueOnSelectedDay = remember(uiState.allTasks, selectedDayMillis) {
        uiState.allTasks
            .filter { DateTimeUtils.isSameDay(it.dueDateMillis, selectedDayMillis) }
            .sortedBy { it.dueDateMillis }
    }
    val tasksAssignedOnSelectedDay = remember(uiState.allTasks, selectedDayMillis) {
        uiState.allTasks.filter {
            DateTimeUtils.isSameDay(it.assignedDateMillis, selectedDayMillis) &&
                !DateTimeUtils.isSameDay(it.dueDateMillis, selectedDayMillis)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Mode Switcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Calendar & Weekly Planner",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = filter.isWeeklyPlannerMode,
                        onClick = { onTogglePlannerMode(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("planner_mode_weekly")
                    ) {
                        Text("Weekly Planner")
                    }
                    SegmentedButton(
                        selected = !filter.isWeeklyPlannerMode,
                        onClick = { onTogglePlannerMode(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("planner_mode_monthly")
                    ) {
                        Text("Monthly Calendar")
                    }
                }
            }
        }

        // Planner Card (Weekly Strip or Monthly Calendar Grid)
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filter.isWeeklyPlannerMode) {
                        // Weekly navigation header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onShiftWeek(-1) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous week"
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${DateTimeUtils.formatShortDate(weekDays.first())} – ${DateTimeUtils.formatShortDate(weekDays.last())}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { onSelectDate(todayMillis) }) {
                                    Text("Jump to Today")
                                }
                            }
                            IconButton(onClick = { onShiftWeek(1) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next week"
                                )
                            }
                        }

                        // 7-day interactive row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            weekDays.forEach { dayMillis ->
                                val dayTasks = uiState.allTasks.filter {
                                    DateTimeUtils.isSameDay(it.dueDateMillis, dayMillis)
                                }
                                val isSelected = DateTimeUtils.isSameDay(dayMillis, selectedDayMillis)
                                val isToday = DateTimeUtils.isSameDay(dayMillis, todayMillis)

                                DayChipCell(
                                    dayLabel = DateTimeUtils.formatDayOfWeekShort(dayMillis),
                                    dayNumber = DateTimeUtils.dayOfMonth(dayMillis).toString(),
                                    tasksForDay = dayTasks,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    onClick = { onSelectDate(dayMillis) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // Monthly Calendar Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onShiftMonth(-1) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous month"
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = DateTimeUtils.formatMonthYear(filter.calendarMonthAnchorMillis),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { onSelectDate(todayMillis) }) {
                                    Text("Today")
                                }
                            }
                            IconButton(onClick = { onShiftMonth(1) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next month"
                                )
                            }
                        }

                        // Weekday headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WEEKDAY_HEADERS.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 6-row month grid
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            monthGrid.chunked(7).forEach { weekRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    weekRow.forEach { (dayMillis, inMonth) ->
                                        val dayTasks = uiState.allTasks.filter {
                                            DateTimeUtils.isSameDay(it.dueDateMillis, dayMillis)
                                        }
                                        val isSelected = DateTimeUtils.isSameDay(dayMillis, selectedDayMillis)
                                        val isToday = DateTimeUtils.isSameDay(dayMillis, todayMillis)

                                        MonthDayCell(
                                            dayNumber = DateTimeUtils.dayOfMonth(dayMillis).toString(),
                                            inCurrentMonth = inMonth,
                                            isSelected = isSelected,
                                            isToday = isToday,
                                            tasksForDay = dayTasks,
                                            onClick = { onSelectDate(dayMillis) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Agenda Header + Schedule Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateTimeUtils.formatFullDate(selectedDayMillis),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${tasksDueOnSelectedDay.size} deadline(s) due",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = { onAddTaskForDate(selectedDayMillis) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("planner_add_for_day_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule")
                }
            }
        }

        // Tasks due on selected day
        if (tasksDueOnSelectedDay.isEmpty() && tasksAssignedOnSelectedDay.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nothing due on ${DateTimeUtils.formatShortDate(selectedDayMillis)}. Tap Schedule to plan an assignment or study block.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(tasksDueOnSelectedDay, key = { "due_${it.id}" }) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = { onToggleTaskCompletion(task) },
                    onClick = { onTaskClick(task) }
                )
            }
            if (tasksAssignedOnSelectedDay.isNotEmpty()) {
                item {
                    Text(
                        text = "Assigned on this date (${tasksAssignedOnSelectedDay.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(tasksAssignedOnSelectedDay, key = { "assigned_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleTaskCompletion(task) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }

        // Weekly Overview Digest in Weekly Mode
        if (filter.isWeeklyPlannerMode) {
            item {
                val weekTasks = uiState.allTasks.filter { task ->
                    weekDays.any { DateTimeUtils.isSameDay(task.dueDateMillis, it) }
                }
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "This Week at a Glance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val pendingWeek = weekTasks.count { !it.isCompleted }
                        val doneWeek = weekTasks.count { it.isCompleted }
                        Text(
                            text = "${weekTasks.size} scheduled activity(ies) · $doneWeek completed · $pendingWeek remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        weekDays.forEach { day ->
                            val count = weekTasks.count { DateTimeUtils.isSameDay(it.dueDateMillis, day) }
                            if (count > 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectDate(day) }
                                        .padding(vertical = 4.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${DateTimeUtils.formatDayOfWeekShort(day)}, ${DateTimeUtils.formatShortDate(day)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (DateTimeUtils.isSameDay(day, selectedDayMillis)) {
                                            FontWeight.Bold
                                        } else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "$count due",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
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
private fun DayChipCell(
    dayLabel: String,
    dayNumber: String,
    tasksForDay: List<AcademicTask>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val fgColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) fgColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = fgColor
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tasksForDay.take(3).forEach { task ->
                val dotColor = when {
                    isSelected -> Color.White
                    task.isOverdue() -> CrimsonOverdue
                    task.isCompleted -> SageTeal
                    else -> subjectColor(task.subjectColorIndex)
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    dayNumber: String,
    inCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    tasksForDay: List<AcademicTask>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    val borderMod = if (isToday && !isSelected) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
    } else Modifier

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                !inCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(5.dp)
        ) {
            tasksForDay.take(3).forEach { task ->
                val dotColor = when {
                    isSelected -> Color.White
                    task.isOverdue() -> CrimsonOverdue
                    task.isCompleted -> SageTeal
                    else -> subjectColor(task.subjectColorIndex)
                }
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}
