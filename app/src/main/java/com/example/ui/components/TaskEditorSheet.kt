package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicTask
import com.example.data.model.ReminderLeadTime
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.theme.SubjectPalette
import com.example.util.DateTimeUtils

private val QUICK_SUBJECT_PRESETS = listOf(
    "Mathematics" to 0,
    "Science" to 1,
    "English" to 2,
    "History" to 7,
    "Computer Science" to 3,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorSheet(
    editingTask: AcademicTask?,
    initialDueDateMillis: Long,
    defaultReminderMinutes: Int,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (
        existingId: Long,
        title: String,
        subjectName: String,
        subjectColorIndex: Int,
        category: TaskCategory,
        priority: TaskPriority,
        description: String,
        assignedDateMillis: Long,
        dueDateMillis: Long,
        reminderLeadMinutes: Int,
        isCompleted: Boolean,
        completedAtMillis: Long?,
    ) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val now = remember { System.currentTimeMillis() }

    var title by remember(editingTask) { mutableStateOf(editingTask?.title ?: "") }
    var subjectName by remember(editingTask) {
        mutableStateOf(editingTask?.subjectName ?: subjects.firstOrNull()?.name ?: "")
    }
    var subjectColorIndex by remember(editingTask) {
        mutableIntStateOf(
            editingTask?.subjectColorIndex
                ?: subjects.firstOrNull()?.colorIndex
                ?: 0
        )
    }
    var category by remember(editingTask) {
        mutableStateOf(editingTask?.category ?: TaskCategory.HOMEWORK)
    }
    var priority by remember(editingTask) {
        mutableStateOf(editingTask?.priority ?: TaskPriority.MEDIUM)
    }
    var description by remember(editingTask) { mutableStateOf(editingTask?.description ?: "") }
    var assignedDateMillis by remember(editingTask) {
        mutableLongStateOf(editingTask?.assignedDateMillis ?: DateTimeUtils.startOfDay(now))
    }
    var dueDateMillis by remember(editingTask, initialDueDateMillis) {
        mutableLongStateOf(
            editingTask?.dueDateMillis ?: DateTimeUtils.combineDateAndTime(
                dateMillis = initialDueDateMillis,
                hourOfDay = 23,
                minute = 59
            )
        )
    }
    var reminderLeadMinutes by remember(editingTask) {
        mutableIntStateOf(editingTask?.reminderLeadMinutes ?: defaultReminderMinutes)
    }
    var showValidationError by remember { mutableStateOf(false) }
    var showAssignedDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var showDueTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingTask == null) "New Academic Task" else "Edit Academic Task",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close editor")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) showValidationError = false
                },
                label = { Text("Task Title *") },
                placeholder = { Text("e.g., Calculus Problem Set 4") },
                singleLine = true,
                isError = showValidationError && title.isBlank(),
                supportingText = if (showValidationError && title.isBlank()) {
                    { Text("Please enter a task title") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            // Subject selection + inline creation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Subject",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { input ->
                        subjectName = input
                        val matched = subjects.firstOrNull { it.name.equals(input.trim(), ignoreCase = true) }
                        if (matched != null) {
                            subjectColorIndex = matched.colorIndex
                        }
                    },
                    label = { Text("Subject / Course") },
                    placeholder = { Text("e.g., Biology, Calculus, Literature") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_subject_input")
                )

                val chipItems = if (subjects.isNotEmpty()) {
                    subjects.map { it.name to it.colorIndex }
                } else {
                    QUICK_SUBJECT_PRESETS
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chipItems.forEach { (name, colorIdx) ->
                        val selected = subjectName.equals(name, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                subjectName = name
                                subjectColorIndex = colorIdx
                            },
                            label = { Text(name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor(colorIdx))
                                )
                            }
                        )
                    }
                }

                // Color swatch picker for new/custom subject
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tag color:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SubjectPalette.forEachIndexed { idx, color ->
                        val selected = subjectColorIndex == idx
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selected) {
                                        Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                                .clickable { subjectColorIndex = idx }
                        )
                    }
                }
            }

            // Activity Type / Category
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Activity Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Priority level
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TaskPriority.entries.forEachIndexed { index, level ->
                        SegmentedButton(
                            selected = priority == level,
                            onClick = { priority = level },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TaskPriority.entries.size
                            ),
                            modifier = Modifier.testTag("priority_option_${level.name.lowercase()}")
                        ) {
                            Text(level.label)
                        }
                    }
                }
            }

            // Dates & Time
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Schedule & Deadlines",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateSelectorButton(
                        label = "Assigned Date",
                        value = DateTimeUtils.formatShortDate(assignedDateMillis),
                        icon = Icons.Filled.EventAvailable,
                        onClick = { showAssignedDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assigned_date_button")
                    )
                    DateSelectorButton(
                        label = "Due Date",
                        value = DateTimeUtils.formatShortDate(dueDateMillis),
                        icon = Icons.Filled.CalendarToday,
                        onClick = { showDueDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("due_date_button")
                    )
                }
                DateSelectorButton(
                    label = "Due Time",
                    value = DateTimeUtils.formatTime(dueDateMillis),
                    icon = Icons.Filled.AccessTime,
                    onClick = { showDueTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("due_time_button")
                )
            }

            // Reminder timing
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Deadline Reminder",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReminderLeadTime.entries.forEach { lead ->
                        FilterChip(
                            selected = reminderLeadMinutes == lead.minutesBefore,
                            onClick = { reminderLeadMinutes = lead.minutesBefore },
                            label = { Text(lead.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes") },
                placeholder = { Text("Chapters to review, submission format, rubric notes...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editingTask != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(editingTask.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete task",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            showValidationError = true
                            return@Button
                        }
                        onSave(
                            editingTask?.id ?: 0L,
                            title,
                            subjectName.ifBlank { "General" },
                            subjectColorIndex,
                            category,
                            priority,
                            description,
                            assignedDateMillis,
                            dueDateMillis,
                            reminderLeadMinutes,
                            editingTask?.isCompleted ?: false,
                            editingTask?.completedAtMillis,
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("save_task_button")
                ) {
                    Text(
                        text = if (editingTask == null) "Add Task" else "Save Changes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showAssignedDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.localMillisToUtcPickerMillis(assignedDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showAssignedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utc ->
                        assignedDateMillis = DateTimeUtils.utcPickerMillisToLocalDay(utc)
                    }
                    showAssignedDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showAssignedDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDueDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.localMillisToUtcPickerMillis(dueDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utc ->
                        val localDay = DateTimeUtils.utcPickerMillisToLocalDay(utc)
                        val (hour, minute) = DateTimeUtils.getHourAndMinute(dueDateMillis)
                        dueDateMillis = DateTimeUtils.combineDateAndTime(localDay, hour, minute)
                    }
                    showDueDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDueTimePicker) {
        val (initHour, initMinute) = DateTimeUtils.getHourAndMinute(dueDateMillis)
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showDueTimePicker = false },
            title = { Text("Select Due Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = DateTimeUtils.combineDateAndTime(
                        dateMillis = dueDateMillis,
                        hourOfDay = timePickerState.hour,
                        minute = timePickerState.minute
                    )
                    showDueTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDueTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DateSelectorButton(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
