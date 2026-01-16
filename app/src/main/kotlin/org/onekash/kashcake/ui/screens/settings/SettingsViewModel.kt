package org.onekash.kashcake.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.onekash.kashcake.data.contacts.ContactSyncManager
import org.onekash.kashcake.data.preferences.KashCakeDataStore
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: KashCakeDataStore,
    private val contactSyncManager: ContactSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                dataStore.defaultReminderDays,
                dataStore.contactsSyncEnabled,
                dataStore.notificationTimeHour,
                dataStore.notificationTimeMinute
            ) { reminderDays, syncEnabled, hour, minute ->
                SettingsUiState(
                    defaultReminderDays = reminderDays,
                    contactsSyncEnabled = syncEnabled,
                    notificationHour = hour,
                    notificationMinute = minute,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setDefaultReminderDays(days: Int) {
        viewModelScope.launch {
            dataStore.setDefaultReminderDays(days)
        }
    }

    fun setContactsSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setContactsSyncEnabled(enabled)
            if (enabled) {
                contactSyncManager.syncBirthdays()
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.setNotificationTime(hour, minute)
        }
    }

    fun syncContactsNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            contactSyncManager.syncBirthdays()
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }
}

data class SettingsUiState(
    val defaultReminderDays: Int = 1,
    val contactsSyncEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false
)
