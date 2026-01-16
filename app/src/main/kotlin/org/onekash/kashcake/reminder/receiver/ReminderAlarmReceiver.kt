package org.onekash.kashcake.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.onekash.kashcake.data.db.dao.BirthdayDao
import org.onekash.kashcake.reminder.ReminderNotificationManager
import org.onekash.kashcake.reminder.ReminderScheduler
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var birthdayDao: BirthdayDao
    @Inject lateinit var notificationManager: ReminderNotificationManager
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_BIRTHDAY_REMINDER) return

        val birthdayId = intent.getLongExtra(ReminderScheduler.EXTRA_BIRTHDAY_ID, -1)
        if (birthdayId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val birthday = birthdayDao.getBirthdayById(birthdayId) ?: return@launch
                notificationManager.showBirthdayReminder(birthday)

                // Schedule next year's reminder
                reminderScheduler.scheduleReminder(birthday)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
