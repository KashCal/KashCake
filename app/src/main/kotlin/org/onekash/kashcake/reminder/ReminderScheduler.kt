package org.onekash.kashcake.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.onekash.kashcake.data.db.dao.BirthdayDao
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.data.preferences.KashCakeDataStore
import org.onekash.kashcake.reminder.receiver.ReminderAlarmReceiver
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val birthdayDao: BirthdayDao,
    private val dataStore: KashCakeDataStore
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_BIRTHDAY_REMINDER = "org.onekash.kashcake.BIRTHDAY_REMINDER"
        const val EXTRA_BIRTHDAY_ID = "birthday_id"
        private const val REQUEST_CODE_BASE = 1000
    }

    suspend fun scheduleReminder(birthday: Birthday) {
        val nextBirthday = birthday.getNextBirthdayDate()
        val reminderDate = nextBirthday.minusDays(birthday.reminderDaysBefore.toLong())

        // Get notification time from preferences
        val hour = dataStore.notificationTimeHour.first()
        val minute = dataStore.notificationTimeMinute.first()

        val reminderDateTime = reminderDate.atTime(hour, minute)
        val reminderInstant = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant()

        // Skip if reminder time already passed
        if (reminderInstant.isBefore(Instant.now())) {
            return
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_BIRTHDAY_REMINDER
            putExtra(EXTRA_BIRTHDAY_ID, birthday.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_BASE + birthday.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderInstant.toEpochMilli(),
            pendingIntent
        )
    }

    fun cancelReminder(birthdayId: Long) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_BIRTHDAY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_BASE + birthdayId).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let { alarmManager.cancel(it) }
    }

    suspend fun rescheduleAll() {
        birthdayDao.getAllBirthdays().first().forEach { birthday ->
            scheduleReminder(birthday)
        }
    }

    suspend fun cancelAllReminders() {
        birthdayDao.getAllBirthdays().first().forEach { birthday ->
            cancelReminder(birthday.id)
        }
    }
}
