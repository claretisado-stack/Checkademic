package com.example.data.repository

import com.example.data.local.SubjectDao
import com.example.data.local.TaskDao
import com.example.data.model.AcademicTask
import com.example.data.model.SubjectEntity
import com.example.data.preferences.ReminderPreferences
import com.example.data.preferences.ReminderSettings
import com.example.notifications.DeadlineScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AcademicRepository(
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao,
    private val reminderPreferences: ReminderPreferences,
    private val deadlineScheduler: DeadlineScheduler,
) {
    val allTasks: Flow<List<AcademicTask>> = taskDao.observeAllTasks()
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.observeAllSubjects()
    val reminderSettings: Flow<ReminderSettings> = reminderPreferences.settingsFlow

    suspend fun saveTask(task: AcademicTask): Long {
        val trimmedSubject = task.subjectName.trim().ifEmpty { "General" }
        val existingSubject = subjectDao.getSubjectByName(trimmedSubject)
        val resolvedSubjectId: Long
        val resolvedColorIndex: Int

        if (existingSubject != null) {
            resolvedSubjectId = existingSubject.id
            resolvedColorIndex = existingSubject.colorIndex
        } else {
            resolvedColorIndex = task.subjectColorIndex
            resolvedSubjectId = subjectDao.insertSubject(
                SubjectEntity(
                    name = trimmedSubject,
                    colorIndex = resolvedColorIndex
                )
            )
        }

        val normalizedTask = task.copy(
            title = task.title.trim(),
            subjectId = resolvedSubjectId,
            subjectName = existingSubject?.name ?: trimmedSubject,
            subjectColorIndex = resolvedColorIndex,
            description = task.description.trim()
        )

        val savedId = if (normalizedTask.id == 0L) {
            taskDao.insertTask(normalizedTask)
        } else {
            taskDao.updateTask(normalizedTask)
            normalizedTask.id
        }

        val settings = reminderPreferences.settingsFlow.first()
        deadlineScheduler.scheduleReminder(
            task = normalizedTask.copy(id = savedId),
            globalRemindersEnabled = settings.remindersEnabled
        )
        return savedId
    }

    suspend fun toggleTaskCompletion(task: AcademicTask) {
        val nextCompleted = !task.isCompleted
        val completedAt = if (nextCompleted) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(task.id, nextCompleted, completedAt)

        val settings = reminderPreferences.settingsFlow.first()
        if (nextCompleted) {
            deadlineScheduler.cancelReminder(task.id)
        } else {
            deadlineScheduler.scheduleReminder(
                task = task.copy(isCompleted = false, completedAtMillis = null),
                globalRemindersEnabled = settings.remindersEnabled
            )
        }
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
        deadlineScheduler.cancelReminder(taskId)
    }

    suspend fun saveSubject(subject: SubjectEntity): Long {
        val trimmedName = subject.name.trim()
        if (subject.id == 0L) {
            val existing = subjectDao.getSubjectByName(trimmedName)
            if (existing != null) {
                val updated = existing.copy(
                    courseCode = subject.courseCode.trim(),
                    colorIndex = subject.colorIndex
                )
                subjectDao.updateSubject(updated)
                taskDao.syncSubjectMetadataForTasks(updated.id, updated.name, updated.colorIndex)
                return updated.id
            }
            return subjectDao.insertSubject(
                subject.copy(name = trimmedName, courseCode = subject.courseCode.trim())
            )
        } else {
            val updated = subject.copy(name = trimmedName, courseCode = subject.courseCode.trim())
            subjectDao.updateSubject(updated)
            taskDao.syncSubjectMetadataForTasks(updated.id, updated.name, updated.colorIndex)
            return updated.id
        }
    }

    suspend fun deleteSubject(subjectId: Long) {
        subjectDao.deleteSubjectById(subjectId)
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        reminderPreferences.setRemindersEnabled(enabled)
        val tasks = taskDao.observeAllTasks().first()
        tasks.forEach { task ->
            if (enabled) {
                deadlineScheduler.scheduleReminder(task, globalRemindersEnabled = true)
            } else {
                deadlineScheduler.cancelReminder(task.id)
            }
        }
    }

    suspend fun setDefaultLeadMinutes(minutes: Int) {
        reminderPreferences.setDefaultLeadMinutes(minutes)
    }
}
