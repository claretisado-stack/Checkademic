package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeadlineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT) ?: "Academic"
        val categoryLabel = intent.getStringExtra(EXTRA_CATEGORY) ?: "Assignment"
        val priorityLabel = intent.getStringExtra(EXTRA_PRIORITY) ?: "Medium"
        val dueDateMillis = intent.getLongExtra(EXTRA_DUE_MILLIS, System.currentTimeMillis())

        DeadlineNotificationHelper.showTaskReminder(
            context = context,
            taskId = taskId,
            title = title,
            subjectName = subjectName,
            categoryLabel = categoryLabel,
            priorityLabel = priorityLabel,
            dueDateMillis = dueDateMillis
        )
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "task_title"
        const val EXTRA_SUBJECT = "task_subject"
        const val EXTRA_CATEGORY = "task_category"
        const val EXTRA_PRIORITY = "task_priority"
        const val EXTRA_DUE_MILLIS = "task_due_millis"
    }
}
