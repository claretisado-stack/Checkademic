package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AcademicTask
import com.example.util.DateTimeUtils

object DeadlineNotificationHelper {
    const val CHANNEL_ID = "checkademic_deadlines"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showTaskReminder(
        context: Context,
        taskId: Long,
        title: String,
        subjectName: String,
        categoryLabel: String,
        priorityLabel: String,
        dueDateMillis: Long,
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dueSummary = DateTimeUtils.formatDateTime(dueDateMillis)
        val body = "$subjectName · $categoryLabel ($priorityLabel priority)\nDue $dueSummary"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Upcoming Deadline: $title")
            .setContentText("$subjectName · Due $dueSummary")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission may have been revoked between check and notify
        }
    }

    fun showSampleTestReminder(context: Context, upcomingTask: AcademicTask?) {
        val title = upcomingTask?.title ?: "Checkademic Deadline Alerts Active"
        val subject = upcomingTask?.subjectName ?: "Study Planner"
        val category = upcomingTask?.category?.label ?: "Reminder"
        val priority = upcomingTask?.priority?.label ?: "High"
        val due = upcomingTask?.dueDateMillis ?: (System.currentTimeMillis() + 3_600_000L)
        showTaskReminder(
            context = context,
            taskId = upcomingTask?.id ?: 9999L,
            title = title,
            subjectName = subject,
            categoryLabel = category,
            priorityLabel = priority,
            dueDateMillis = due
        )
    }

    const val EXTRA_TASK_ID = "extra_task_id"
}
