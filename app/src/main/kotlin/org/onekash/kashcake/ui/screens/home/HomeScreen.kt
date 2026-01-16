package org.onekash.kashcake.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.onekash.kashcake.R
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.ui.components.BirthdayCard
import org.onekash.kashcake.ui.components.BirthdayQuickViewSheet
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedBirthday by remember { mutableStateOf<Birthday?>(null) }
    val listState = rememberLazyListState()

    // Calculate initial scroll position to show "Today" or "Upcoming" first
    val thisMonthCount = uiState.thisMonthPastBirthdays.size
    val initialIndex = if (thisMonthCount > 0) thisMonthCount + 1 else 0 // +1 for section header

    // Scroll to today/upcoming section on first load
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && thisMonthCount > 0) {
            listState.scrollToItem(initialIndex)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cake Days",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.contactsSyncEnabled) {
            // Show unicorn mascot when sync not enabled
            EmptyState(
                onEnableSync = onOpenSettings,
                modifier = Modifier.padding(padding)
            )
        } else if (uiState.thisMonthPastBirthdays.isEmpty() &&
                   uiState.todayBirthdays.isEmpty() &&
                   uiState.upcomingBirthdays.isEmpty()) {
            // Sync enabled but no birthdays found
            NoBirthdaysState(modifier = Modifier.padding(padding))
        } else {
            val currentMonthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // This month (past birthdays - scroll up to see)
                if (uiState.thisMonthPastBirthdays.isNotEmpty()) {
                    item {
                        Text(
                            "$currentMonthName (Earlier)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.thisMonthPastBirthdays, key = { "past_${it.id}" }) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            isToday = false,
                            isPast = true,
                            onClick = { selectedBirthday = birthday }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                // Today's birthdays
                if (uiState.todayBirthdays.isNotEmpty()) {
                    item {
                        Text(
                            "Today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(uiState.todayBirthdays, key = { "today_${it.id}" }) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            isToday = true,
                            isPast = false,
                            onClick = { selectedBirthday = birthday }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                // Upcoming birthdays grouped by month
                if (uiState.upcomingBirthdays.isNotEmpty()) {
                    val currentYear = LocalDate.now().year
                    val groupedByMonth = uiState.upcomingBirthdays.groupBy { birthday ->
                        val nextBirthday = birthday.getNextBirthdayDate()
                        // Key: "Month" for current year, "Month Year" for future years
                        val monthName = nextBirthday.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        if (nextBirthday.year == currentYear) monthName else "$monthName ${nextBirthday.year}"
                    }

                    groupedByMonth.forEach { (monthLabel, birthdays) ->
                        item(key = "month_$monthLabel") {
                            Text(
                                monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(birthdays, key = { "upcoming_${it.id}" }) { birthday ->
                            BirthdayCard(
                                birthday = birthday,
                                isToday = false,
                                isPast = false,
                                onClick = { selectedBirthday = birthday }
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick View Sheet
    selectedBirthday?.let { birthday ->
        BirthdayQuickViewSheet(
            birthday = birthday,
            onDismiss = { selectedBirthday = null }
        )
    }
}

@Composable
private fun EmptyState(
    onEnableSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Unicorn mascot
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "KashCake Unicorn",
                modifier = Modifier.size(160.dp),
                alignment = Alignment.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Never Miss",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "A Birthday",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Import birthdays from your contacts",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onEnableSync,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "Enable Contact Sync",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun NoBirthdaysState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "KashCake Unicorn",
                modifier = Modifier.size(120.dp),
                alignment = Alignment.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "No birthdays yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Add birthdays to your contacts in the Contacts app",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
