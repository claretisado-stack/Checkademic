package com.example

import android.app.Application
import com.example.data.local.CheckademicDatabase
import com.example.data.preferences.ReminderPreferences
import com.example.data.repository.AcademicRepository
import com.example.notifications.DeadlineNotificationHelper
import com.example.notifications.DeadlineScheduler

class CheckademicApp : Application() {
    val database by lazy { CheckademicDatabase.getInstance(this) }
    val reminderPreferences by lazy { ReminderPreferences(this) }
    val deadlineScheduler by lazy { DeadlineScheduler(this) }
    val repository by lazy {
        AcademicRepository(
            taskDao = database.taskDao(),
            subjectDao = database.subjectDao(),
            reminderPreferences = reminderPreferences,
            deadlineScheduler = deadlineScheduler,
        )
    }

    override fun onCreate() {
        super.onCreate()
        DeadlineNotificationHelper.createNotificationChannel(this)
    }
}
