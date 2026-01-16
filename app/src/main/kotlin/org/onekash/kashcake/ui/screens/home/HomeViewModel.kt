package org.onekash.kashcake.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.onekash.kashcake.data.contacts.ContactSyncManager
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.data.preferences.KashCakeDataStore
import org.onekash.kashcake.domain.BirthdayRepository
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BirthdayRepository,
    private val contactSyncManager: ContactSyncManager,
    private val dataStore: KashCakeDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadBirthdays()
    }

    private fun loadBirthdays() {
        viewModelScope.launch {
            combine(
                repository.getUpcomingBirthdays(),
                dataStore.contactsSyncEnabled
            ) { birthdays, syncEnabled ->
                val currentMonth = LocalDate.now().monthValue
                val currentDay = LocalDate.now().dayOfMonth

                // This month birthdays that already passed (scroll up to see)
                val thisMonthPast = birthdays.filter {
                    it.month == currentMonth && it.day < currentDay
                }.sortedBy { it.day }

                val today = birthdays.filter { it.isBirthdayToday() }
                // Exclude birthdays already shown in thisMonthPast
                val upcoming = birthdays.filter {
                    !it.isBirthdayToday() && !(it.month == currentMonth && it.day < currentDay)
                }

                HomeUiState(
                    thisMonthPastBirthdays = thisMonthPast,
                    todayBirthdays = today,
                    upcomingBirthdays = upcoming,
                    isLoading = false,
                    contactsSyncEnabled = syncEnabled
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun syncContacts() {
        // Trigger sync via WorkManager - UI updates via Flow when database changes
        contactSyncManager.triggerSync()
    }
}

data class HomeUiState(
    val thisMonthPastBirthdays: List<Birthday> = emptyList(),
    val todayBirthdays: List<Birthday> = emptyList(),
    val upcomingBirthdays: List<Birthday> = emptyList(),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val contactsSyncEnabled: Boolean = false
)
