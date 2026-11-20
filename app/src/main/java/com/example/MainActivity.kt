package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notifications.DeadlineNotificationHelper
import com.example.ui.CheckademicRoot
import com.example.ui.theme.CheckademicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialTaskId = intent?.getLongExtra(DeadlineNotificationHelper.EXTRA_TASK_ID, -1L) ?: -1L
        setContent {
            CheckademicTheme {
                CheckademicRoot(initialTaskIdFromNotification = initialTaskId)
            }
        }
    }
}
