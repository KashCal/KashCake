package org.onekash.kashcake.data.contacts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.onekash.kashcake.data.preferences.KashCakeDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: KashCakeDataStore
) {
    private var observer: ContactBirthdayObserver? = null
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun initialize() {
        scope.launch {
            dataStore.contactsSyncEnabled.collect { enabled ->
                if (enabled) {
                    registerObserver()
                    triggerSync()
                } else {
                    unregisterObserver()
                }
            }
        }
    }

    private fun registerObserver() {
        if (observer != null) return

        observer = ContactBirthdayObserver(
            handler = handler,
            scope = scope
        ) {
            triggerSync()
        }

        context.contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            observer!!
        )
    }

    private fun unregisterObserver() {
        observer?.let {
            context.contentResolver.unregisterContentObserver(it)
            it.cancelPending()
        }
        observer = null
    }

    /**
     * Triggers a birthday sync via WorkManager.
     * Uses REPLACE policy to cancel any pending sync and start fresh.
     */
    fun triggerSync() {
        val syncRequest = OneTimeWorkRequestBuilder<BirthdaySyncWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            BirthdaySyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
