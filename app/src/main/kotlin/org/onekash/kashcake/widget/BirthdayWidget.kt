package org.onekash.kashcake.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.onekash.kashcake.MainActivity
import org.onekash.kashcake.data.db.KashCakeDatabase

class BirthdayWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KashCakeDatabase.getInstance(context)
        val widgetRepo = WidgetDataRepository(database.birthdayDao())
        val upcoming = widgetRepo.getUpcomingBirthdays(limit = 5)

        provideContent {
            GlanceTheme {
                BirthdayWidgetContent(birthdays = upcoming)
            }
        }
    }
}

@Composable
private fun BirthdayWidgetContent(birthdays: List<WidgetDataRepository.WidgetBirthday>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Upcoming Birthdays",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        if (birthdays.isEmpty()) {
            Text(
                "No upcoming birthdays",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            birthdays.take(4).forEach { birthday ->
                BirthdayWidgetRow(birthday)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun BirthdayWidgetRow(birthday: WidgetDataRepository.WidgetBirthday) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            birthday.name,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            birthday.daysUntilText,
            style = TextStyle(
                fontSize = 11.sp,
                color = if (birthday.isToday) {
                    GlanceTheme.colors.primary
                } else {
                    GlanceTheme.colors.onSurfaceVariant
                }
            )
        )
    }
}
