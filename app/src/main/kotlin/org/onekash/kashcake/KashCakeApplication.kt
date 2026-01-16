package org.onekash.kashcake

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.onekash.kashcake.data.contacts.ContactSyncManager
import org.onekash.kashcake.reminder.ReminderNotificationManager
import org.onekash.kashcake.reminder.ReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class KashCakeApplication : Application() {

    @Inject lateinit var contactSyncManager: ContactSyncManager
    @Inject lateinit var notificationManager: ReminderNotificationManager
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        notificationManager.createNotificationChannel()

        // Set up callback for contact sync to reschedule reminders
        contactSyncManager.onSyncComplete = {
            reminderScheduler.rescheduleAll()
        }

        // Initialize contact sync observer
        contactSyncManager.initialize()
    }
}
