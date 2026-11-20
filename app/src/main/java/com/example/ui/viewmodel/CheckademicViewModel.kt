package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.CheckademicApp
import com.example.data.model.AcademicTask
import com.example.data.model.ReminderLeadTime
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatusFilter
import com.example.data.preferences.ReminderSettings
import com.example.data.repository.AcademicRepository
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectProgress(
    val subject: SubjectEntity,
    val totalCount: Int,
    val completedCount: Int,
    val overdueCount: Int,
) {
    val pendingCount: Int get() = (totalCount - completedCount).coerceAtLeast(0)
    val completionFraction: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
}

data class DashboardStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val upcomingTasks: Int = 0,
    val completionPercentage: Float = 0f,
    val dueSoonTasks: List<AcademicTask> = emptyList(),
    val overdueHighlightTasks: List<AcademicTask> = emptyList(),
    val subjectProgress: List<SubjectProgress> = emptyList(),
)

data class FilterAndPlannerState(
    val statusFilter: TaskStatusFilter = TaskStatusFilter.ALL,
    val selectedSubjectId: Long? = null,
    val selectedPriority: TaskPriority? = null,
    val searchQuery: String = "",
    val selectedDateMillis: Long = DateTimeUtils.startOfDay(System.currentTimeMillis()),
    val calendarMonthAnchorMillis: Long = DateTimeUtils.startOfDay(System.currentTimeMillis()),
    val isWeeklyPlannerMode: Boolean = true,
)

data class CheckademicUiState(
    val isLoading: Boolean = true,
    val allTasks: List<AcademicTask> = emptyList(),
    val filteredPendingTasks: List<AcademicTask> = emptyList(),
    val filteredCompletedTasks: List<AcademicTask> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val stats: DashboardStats = DashboardStats(),
    val filterState: FilterAndPlannerState = FilterAndPlannerState(),
    val reminderSettings: ReminderSettings = ReminderSettings(),
)

class CheckademicViewModel(
    private val repository: AcademicRepository,
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterAndPlannerState())

    val uiState: StateFlow<CheckademicUiState> = combine(
        repository.allTasks,
        repository.allSubjects,
        repository.reminderSettings,
        _filterState,
    ) { tasks, subjects, settings, filter ->
        val now = System.currentTimeMillis()
        val total = tasks.size
        val completedList = tasks.filter { it.isCompleted }
        val pendingList = tasks.filter { !it.isCompleted }
        val overdueList = pendingList.filter { it.isOverdue(now) }.sortedBy { it.dueDateMillis }
        val upcomingList = pendingList.filter { !it.isOverdue(now) }.sortedBy { it.dueDateMillis }
        val dueSoonList = upcomingList.filter { it.isDueSoon(now) }
        val pct = if (total == 0) 0f else (completedList.size.toFloat() / total.toFloat()) * 100f

        val subjectProgress = subjects.map { subject ->
            val subTasks = tasks.filter {
                it.subjectId == subject.id ||
                    it.subjectName.equals(subject.name, ignoreCase = true)
            }
            SubjectProgress(
                subject = subject,
                totalCount = subTasks.size,
                completedCount = subTasks.count { it.isCompleted },
                overdueCount = subTasks.count { it.isOverdue(now) },
            )
        }.sortedByDescending { it.totalCount }

        val query = filter.searchQuery.trim()
        val matchingTasks = tasks.filter { task ->
            val matchesQuery = query.isEmpty() ||
                task.title.contains(query, ignoreCase = true) ||
                task.subjectName.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.category.label.contains(query, ignoreCase = true)
            val matchesSubject = filter.selectedSubjectId == null ||
                task.subjectId == filter.selectedSubjectId
            val matchesPriority = filter.selectedPriority == null ||
                task.priority == filter.selectedPriority
            val matchesStatus = when (filter.statusFilter) {
                TaskStatusFilter.ALL -> true
                TaskStatusFilter.UPCOMING -> !task.isCompleted && !task.isOverdue(now)
                TaskStatusFilter.OVERDUE -> task.isOverdue(now)
                TaskStatusFilter.COMPLETED -> task.isCompleted
            }
            matchesQuery && matchesSubject && matchesPriority && matchesStatus
        }

        val filteredPending = matchingTasks
            .filter { !it.isCompleted }
            .sortedWith(
                compareBy<AcademicTask> { !it.isOverdue(now) }
                    .thenBy { it.dueDateMillis }
                    .thenBy { it.priority.sortOrder }
            )

        val filteredCompleted = matchingTasks
            .filter { it.isCompleted }
            .sortedByDescending { it.completedAtMillis ?: it.dueDateMillis }

        CheckademicUiState(
            isLoading = false,
            allTasks = tasks,
            filteredPendingTasks = filteredPending,
            filteredCompletedTasks = filteredCompleted,
            subjects = subjects,
            stats = DashboardStats(
                totalTasks = total,
                completedTasks = completedList.size,
                pendingTasks = pendingList.size,
                overdueTasks = overdueList.size,
                upcomingTasks = upcomingList.size,
                completionPercentage = pct,
                dueSoonTasks = dueSoonList,
                overdueHighlightTasks = overdueList,
                subjectProgress = subjectProgress,
            ),
            filterState = filter,
            reminderSettings = settings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CheckademicUiState(),
    )

    fun saveTask(
        existingId: Long = 0L,
        title: String,
        subjectName: String,
        subjectColorIndex: Int,
        category: TaskCategory,
        priority: TaskPriority,
        description: String,
        assignedDateMillis: Long,
        dueDateMillis: Long,
        reminderLeadMinutes: Int,
        isCompleted: Boolean = false,
        completedAtMillis: Long? = null,
    ) {
        viewModelScope.launch {
            repository.saveTask(
                AcademicTask(
                    id = existingId,
                    title = title,
                    subjectName = subjectName,
                    subjectColorIndex = subjectColorIndex,
                    category = category,
                    priority = priority,
                    description = description,
                    assignedDateMillis = assignedDateMillis,
                    dueDateMillis = dueDateMillis,
                    isCompleted = isCompleted,
                    completedAtMillis = completedAtMillis,
                    reminderLeadMinutes = reminderLeadMinutes,
                )
            )
        }
    }

    fun toggleTaskCompletion(task: AcademicTask) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun restoreTask(task: AcademicTask) {
        viewModelScope.launch {
            repository.saveTask(task.copy(id = 0L))
        }
    }

    fun saveSubject(existingId: Long = 0L, name: String, courseCode: String, colorIndex: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveSubject(
                SubjectEntity(
                    id = existingId,
                    name = name,
                    courseCode = courseCode,
                    colorIndex = colorIndex,
                )
            )
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
            _filterState.update { state ->
                if (state.selectedSubjectId == subjectId) {
                    state.copy(selectedSubjectId = null)
                } else state
            }
        }
    }

    fun setStatusFilter(filter: TaskStatusFilter) {
        _filterState.update { it.copy(statusFilter = filter) }
    }

    fun setSubjectFilter(subjectId: Long?) {
        _filterState.update { state ->
            state.copy(selectedSubjectId = if (state.selectedSubjectId == subjectId) null else subjectId)
        }
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _filterState.update { state ->
            state.copy(selectedPriority = if (state.selectedPriority == priority) null else priority)
        }
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun clearAllFilters() {
        _filterState.update {
            it.copy(
                statusFilter = TaskStatusFilter.ALL,
                selectedSubjectId = null,
                selectedPriority = null,
                searchQuery = "",
            )
        }
    }

    fun selectPlannerDate(dateMillis: Long) {
        val dayStart = DateTimeUtils.startOfDay(dateMillis)
        _filterState.update {
            it.copy(
                selectedDateMillis = dayStart,
                calendarMonthAnchorMillis = dayStart,
            )
        }
    }

    fun shiftPlannerMonth(deltaMonths: Int) {
        _filterState.update { state ->
            val shifted = DateTimeUtils.addMonths(state.calendarMonthAnchorMillis, deltaMonths)
            state.copy(calendarMonthAnchorMillis = shifted)
        }
    }

    fun shiftPlannerWeek(deltaWeeks: Int) {
        _filterState.update { state ->
            val shifted = DateTimeUtils.addDays(state.selectedDateMillis, deltaWeeks * 7)
            state.copy(
                selectedDateMillis = shifted,
                calendarMonthAnchorMillis = shifted,
            )
        }
    }

    fun setWeeklyPlannerMode(isWeekly: Boolean) {
        _filterState.update { it.copy(isWeeklyPlannerMode = isWeekly) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRemindersEnabled(enabled)
        }
    }

    fun setDefaultLeadTime(leadTime: ReminderLeadTime) {
        viewModelScope.launch {
            repository.setDefaultLeadMinutes(leadTime.minutesBefore)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CheckademicApp
                CheckademicViewModel(app.repository)
            }
        }
    }
}
