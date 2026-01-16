package org.onekash.kashcake

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.onekash.kashcake.data.contacts.ContactSyncManager
import org.onekash.kashcake.reminder.ReminderNotificationManager
import javax.inject.Inject

@HiltAndroidApp
class KashCakeApplication : Application(), Configuration.Provider {

    @Inject lateinit var contactSyncManager: ContactSyncManager
    @Inject lateinit var notificationManager: ReminderNotificationManager
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        notificationManager.createNotificationChannel()

        // Initialize contact sync observer
        contactSyncManager.initialize()
    }
}
