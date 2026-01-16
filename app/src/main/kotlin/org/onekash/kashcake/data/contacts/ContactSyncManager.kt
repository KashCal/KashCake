package org.onekash.kashcake.data.contacts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.onekash.kashcake.data.db.dao.BirthdayDao
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.data.preferences.KashCakeDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importer: ContactBirthdayImporter,
    private val birthdayDao: BirthdayDao,
    private val dataStore: KashCakeDataStore
) {
    private var observer: ContactBirthdayObserver? = null
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Callback to reschedule reminders after sync
    var onSyncComplete: (suspend () -> Unit)? = null

    fun initialize() {
        scope.launch {
            dataStore.contactsSyncEnabled.collect { enabled ->
                if (enabled) {
                    registerObserver()
                    syncBirthdays()
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
            scope.launch { syncBirthdays() }
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

    suspend fun syncBirthdays(): SyncResult {
        val contactBirthdays = importer.getContactsWithBirthdays()
        val existingBirthdays = birthdayDao.getContactLinkedBirthdays()
        val existingByKey = existingBirthdays.associateBy { it.contactLookupKey }

        var added = 0
        var updated = 0
        var deleted = 0

        // Add or update
        for (contact in contactBirthdays) {
            val existing = existingByKey[contact.lookupKey]
            if (existing != null) {
                // Update if changed
                if (existing.name != contact.displayName ||
                    existing.month != contact.month ||
                    existing.day != contact.day ||
                    existing.year != contact.year ||
                    existing.photoUri != contact.photoUri
                ) {
                    birthdayDao.update(
                        existing.copy(
                            name = contact.displayName,
                            month = contact.month,
                            day = contact.day,
                            year = contact.year,
                            photoUri = contact.photoUri,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    updated++
                }
            } else {
                // Add new
                val defaultDays = dataStore.defaultReminderDays.first()
                birthdayDao.insert(
                    Birthday(
                        name = contact.displayName,
                        month = contact.month,
                        day = contact.day,
                        year = contact.year,
                        photoUri = contact.photoUri,
                        contactLookupKey = contact.lookupKey,
                        reminderDaysBefore = defaultDays
                    )
                )
                added++
            }
        }

        // Delete orphaned (contact was deleted) - convert to manual birthday
        val validKeys = contactBirthdays.map { it.lookupKey }
        val orphaned = if (validKeys.isEmpty()) {
            // If no contacts have birthdays, all linked birthdays are orphaned
            birthdayDao.getAllContactLinkedBirthdaysForOrphanCheck()
        } else {
            birthdayDao.getOrphanedContactBirthdays(validKeys)
        }
        for (birthday in orphaned) {
            // Convert to manual birthday instead of deleting
            birthdayDao.update(
                birthday.copy(
                    contactLookupKey = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
            deleted++
        }

        // Reschedule all reminders
        onSyncComplete?.invoke()

        dataStore.setLastContactSync(System.currentTimeMillis())

        return SyncResult(added, updated, deleted)
    }

    data class SyncResult(val added: Int, val updated: Int, val deleted: Int)
}
