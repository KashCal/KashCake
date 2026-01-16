package org.onekash.kashcake.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kashcake_prefs")

@Singleton
class KashCakeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_DEFAULT_REMINDER_DAYS = intPreferencesKey("default_reminder_days")
        private val KEY_CONTACTS_SYNC_ENABLED = booleanPreferencesKey("contacts_sync_enabled")
        private val KEY_LAST_CONTACT_SYNC = longPreferencesKey("last_contact_sync")
        private val KEY_NOTIFICATION_TIME_HOUR = intPreferencesKey("notification_time_hour")
        private val KEY_NOTIFICATION_TIME_MINUTE = intPreferencesKey("notification_time_minute")
        private val KEY_CUSTOM_BACKGROUND_URI = stringPreferencesKey("custom_background_uri")
        private val KEY_USE_DEVICE_WALLPAPER = booleanPreferencesKey("use_device_wallpaper")
    }

    val defaultReminderDays: Flow<Int> = dataStore.data.map { it[KEY_DEFAULT_REMINDER_DAYS] ?: 1 }
    val contactsSyncEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_CONTACTS_SYNC_ENABLED] ?: false }
    val lastContactSync: Flow<Long> = dataStore.data.map { it[KEY_LAST_CONTACT_SYNC] ?: 0L }
    val notificationTimeHour: Flow<Int> = dataStore.data.map { it[KEY_NOTIFICATION_TIME_HOUR] ?: 9 }
    val notificationTimeMinute: Flow<Int> = dataStore.data.map { it[KEY_NOTIFICATION_TIME_MINUTE] ?: 0 }
    val customBackgroundUri: Flow<String?> = dataStore.data.map { it[KEY_CUSTOM_BACKGROUND_URI] }
    val useDeviceWallpaper: Flow<Boolean> = dataStore.data.map { it[KEY_USE_DEVICE_WALLPAPER] ?: true }

    suspend fun setDefaultReminderDays(days: Int) {
        dataStore.edit { it[KEY_DEFAULT_REMINDER_DAYS] = days }
    }

    suspend fun setContactsSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_CONTACTS_SYNC_ENABLED] = enabled }
    }

    suspend fun setLastContactSync(timestamp: Long) {
        dataStore.edit { it[KEY_LAST_CONTACT_SYNC] = timestamp }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[KEY_NOTIFICATION_TIME_HOUR] = hour
            it[KEY_NOTIFICATION_TIME_MINUTE] = minute
        }
    }

    suspend fun setCustomBackgroundUri(uri: String?) {
        dataStore.edit {
            if (uri != null) {
                it[KEY_CUSTOM_BACKGROUND_URI] = uri
                it[KEY_USE_DEVICE_WALLPAPER] = false
            } else {
                it.remove(KEY_CUSTOM_BACKGROUND_URI)
            }
        }
    }

    suspend fun setUseDeviceWallpaper(use: Boolean) {
        dataStore.edit {
            it[KEY_USE_DEVICE_WALLPAPER] = use
            if (use) {
                it.remove(KEY_CUSTOM_BACKGROUND_URI)
            }
        }
    }
}
