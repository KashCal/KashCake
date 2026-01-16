package org.onekash.kashcake.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.onekash.kashcake.MainActivity
import org.onekash.kashcake.R
import org.onekash.kashcake.data.db.entity.Birthday
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "birthday_reminders"
        const val CHANNEL_NAME = "Birthday Reminders"
        private const val NOTIFICATION_ID_BASE = 2000
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming birthdays"
            enableVibration(true)
            enableLights(true)
            lightColor = Color.BLUE
            // Show full content on lock screen (non-confidential)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun showBirthdayReminder(birthday: Birthday) {
        val daysUntil = birthday.daysUntilBirthday()
        val ageText = birthday.ageOnNextBirthday()?.let { " (turning $it)" } ?: ""

        val title = when {
            daysUntil == 0L -> "${birthday.name}'s Birthday Today!"
            daysUntil == 1L -> "${birthday.name}'s Birthday Tomorrow"
            else -> "${birthday.name}'s Birthday in $daysUntil days"
        }

        val text = "${birthday.name}$ageText - ${birthday.month}/${birthday.day}"

        // Open app intent - explicit intent for security
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("birthday_id", birthday.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            birthday.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            (NOTIFICATION_ID_BASE + birthday.id).toInt(),
            notification
        )
    }
}
