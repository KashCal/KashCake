package org.onekash.kashcake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.domain.BirthdayUtils

@Composable
fun BirthdayCard(
    birthday: Birthday,
    isToday: Boolean,
    isPast: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        isPast -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentAlpha = if (isPast) 0.7f else 1f

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo or initial
            if (birthday.photoUri != null) {
                AsyncImage(
                    model = birthday.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    alpha = contentAlpha
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPast) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        birthday.name.first().uppercase(),
                        color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Name and date info
            Column(modifier = Modifier.weight(1f)) {
                // Name only on first line
                Text(
                    birthday.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                           else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Date and age on second line
                val age = if (isPast) birthday.ageThisYear() else birthday.ageOnNextBirthday()
                val dateText = BirthdayUtils.formatBirthdayDate(birthday.month, birthday.day)
                val secondLine = if (age != null) {
                    "$dateText · ${BirthdayUtils.getOrdinalSuffix(age)}"
                } else {
                    dateText
                }
                Text(
                    secondLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Days until badge (not shown for past birthdays)
            if (!isPast) {
                Spacer(Modifier.width(12.dp))

                val daysText = if (isToday) "Today!" else BirthdayUtils.formatDaysUntil(birthday.daysUntilBirthday())
                val badgeContainerColor = if (isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
                val badgeContentColor = if (isToday) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }

                Surface(
                    color = badgeContainerColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        daysText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeContentColor
                    )
                }
            }
        }
    }
}
