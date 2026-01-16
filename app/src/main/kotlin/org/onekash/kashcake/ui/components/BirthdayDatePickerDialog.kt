package org.onekash.kashcake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BirthdayDatePickerDialog(
    initialMonth: Int,
    initialDay: Int,
    initialYear: Int?,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, day: Int, year: Int?) -> Unit
) {
    var month by remember { mutableIntStateOf(initialMonth) }
    var day by remember { mutableIntStateOf(initialDay) }
    var yearText by remember { mutableStateOf(initialYear?.toString() ?: "") }

    val monthNames = Month.entries.map {
        it.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    val maxDay = try {
        val year = yearText.toIntOrNull() ?: 2024 // Use leap year default for Feb
        YearMonth.of(year, month).lengthOfMonth()
    } catch (e: Exception) {
        31
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Birthday") },
        text = {
            Column {
                // Month selector
                Text(
                    "Month",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                MonthDropdown(
                    selectedMonth = month,
                    onMonthSelected = {
                        month = it
                        // Adjust day if needed
                        val newMaxDay = try {
                            val year = yearText.toIntOrNull() ?: 2024
                            YearMonth.of(year, it).lengthOfMonth()
                        } catch (e: Exception) {
                            31
                        }
                        if (day > newMaxDay) day = newMaxDay
                    },
                    monthNames = monthNames
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Day input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Day",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = day.toString(),
                            onValueChange = { value ->
                                val newDay = value.filter { it.isDigit() }.toIntOrNull()
                                if (newDay != null && newDay in 1..maxDay) {
                                    day = newDay
                                } else if (value.isEmpty()) {
                                    day = 1
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Year input (optional)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Year (optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = yearText,
                            onValueChange = { value ->
                                val filtered = value.filter { it.isDigit() }.take(4)
                                yearText = filtered
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("YYYY") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val year = yearText.toIntOrNull()?.takeIf { it > 1900 }
                    onConfirm(month, day, year)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdown(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    monthNames: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = monthNames[selectedMonth - 1],
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            monthNames.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onMonthSelected(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}
