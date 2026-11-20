package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ReminderLeadTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "checkademic_prefs")

data class ReminderSettings(
    val remindersEnabled: Boolean = true,
    val defaultLeadMinutes: Int = ReminderLeadTime.ONE_DAY.minutesBefore,
)

class ReminderPreferences(private val context: Context) {
    private val keyRemindersEnabled = booleanPreferencesKey("reminders_enabled")
    private val keyDefaultLeadMinutes = intPreferencesKey("default_lead_minutes")

    val settingsFlow: Flow<ReminderSettings> = context.dataStore.data.map { prefs ->
        ReminderSettings(
            remindersEnabled = prefs[keyRemindersEnabled] ?: true,
            defaultLeadMinutes = prefs[keyDefaultLeadMinutes] ?: ReminderLeadTime.ONE_DAY.minutesBefore,
        )
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[keyRemindersEnabled] = enabled
        }
    }

    suspend fun setDefaultLeadMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[keyDefaultLeadMinutes] = minutes
        }
    }
}
