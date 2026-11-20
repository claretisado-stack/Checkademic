package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.model.AcademicTask

class DeadlineScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(task: AcademicTask, globalRemindersEnabled: Boolean) {
        cancelReminder(task.id)
        if (!globalRemindersEnabled || task.isCompleted || task.reminderLeadMinutes < 0) return

        val triggerAtMillis = task.dueDateMillis - (task.reminderLeadMinutes * 60_000L)
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val pendingIntent = buildPendingIntent(task)
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancelReminder(taskId: Long) {
        val intent = Intent(context, DeadlineReminderReceiver::class.java)
        val existing = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existing != null) {
            alarmManager.cancel(existing)
            existing.cancel()
        }
    }

    private fun buildPendingIntent(task: AcademicTask): PendingIntent {
        val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            putExtra(DeadlineReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(DeadlineReminderReceiver.EXTRA_TITLE, task.title)
            putExtra(DeadlineReminderReceiver.EXTRA_SUBJECT, task.subjectName)
            putExtra(DeadlineReminderReceiver.EXTRA_CATEGORY, task.category.label)
            putExtra(DeadlineReminderReceiver.EXTRA_PRIORITY, task.priority.label)
            putExtra(DeadlineReminderReceiver.EXTRA_DUE_MILLIS, task.dueDateMillis)
        }
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
