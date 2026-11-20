package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory(val label: String) {
    HOMEWORK("Homework"),
    ASSIGNMENT("Assignment"),
    PROJECT("Project"),
    QUIZ("Quiz"),
    EXAM("Examination"),
    OTHER("Activity"),
}

enum class TaskPriority(val label: String, val sortOrder: Int) {
    HIGH("High", 0),
    MEDIUM("Medium", 1),
    LOW("Low", 2),
}

enum class ReminderLeadTime(val label: String, val minutesBefore: Int) {
    NONE("No reminder", -1),
    AT_DUE("At due time", 0),
    ONE_HOUR("1 hour before", 60),
    THREE_HOURS("3 hours before", 180),
    ONE_DAY("1 day before", 1440),
    TWO_DAYS("2 days before", 2880);

    companion object {
        fun fromMinutes(minutes: Int): ReminderLeadTime =
            entries.firstOrNull { it.minutesBefore == minutes } ?: ONE_DAY
    }
}

enum class TaskStatusFilter(val label: String) {
    ALL("All"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue"),
    COMPLETED("Completed"),
}

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val courseCode: String = "",
    val colorIndex: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "academic_tasks")
data class AcademicTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val subjectId: Long = 0L,
    val subjectName: String,
    val subjectColorIndex: Int = 0,
    val category: TaskCategory = TaskCategory.HOMEWORK,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val description: String = "",
    val assignedDateMillis: Long,
    val dueDateMillis: Long,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val reminderLeadMinutes: Int = ReminderLeadTime.ONE_DAY.minutesBefore,
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    fun isOverdue(nowMillis: Long = System.currentTimeMillis()): Boolean =
        !isCompleted && dueDateMillis < nowMillis

    fun isDueSoon(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (isCompleted || dueDateMillis < nowMillis) return false
        val hours48Millis = 48L * 60L * 60L * 1000L
        return (dueDateMillis - nowMillis) <= hours48Millis
    }
}
