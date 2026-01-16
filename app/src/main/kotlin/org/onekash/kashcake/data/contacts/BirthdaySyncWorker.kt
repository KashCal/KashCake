package org.onekash.kashcake.data.contacts

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.onekash.kashcake.data.db.dao.BirthdayDao
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.data.preferences.KashCakeDataStore
import org.onekash.kashcake.reminder.ReminderScheduler

@HiltWorker
class BirthdaySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val importer: ContactBirthdayImporter,
    private val birthdayDao: BirthdayDao,
    private val dataStore: KashCakeDataStore,
    private val reminderScheduler: ReminderScheduler
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "BirthdaySyncWorker"
        const val WORK_NAME = "birthday_sync"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting birthday sync (attempt ${runAttemptCount + 1})")

        return try {
            val result = syncBirthdays()
            Log.d(TAG, "Sync complete: added=${result.added}, updated=${result.updated}, deleted=${result.deleted}")
            Result.success()
        } catch (e: SecurityException) {
            // Permission denied - don't retry
            Log.e(TAG, "Permission denied", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncBirthdays(): SyncResult {
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

        // Delete orphaned entries (contact was deleted or is a duplicate from another source)
        // Contacts are the single source of truth - no manual entries from sync
        val validKeys = contactBirthdays.map { it.lookupKey }
        val orphaned = if (validKeys.isEmpty()) {
            birthdayDao.getAllContactLinkedBirthdaysForOrphanCheck()
        } else {
            birthdayDao.getOrphanedContactBirthdays(validKeys)
        }
        for (birthday in orphaned) {
            // Cancel scheduled reminder before deleting
            reminderScheduler.cancelReminder(birthday.id)
            birthdayDao.delete(birthday)
            deleted++
        }

        // Reschedule all reminders after sync
        reminderScheduler.rescheduleAll()

        dataStore.setLastContactSync(System.currentTimeMillis())

        return SyncResult(added, updated, deleted)
    }

    data class SyncResult(val added: Int, val updated: Int, val deleted: Int)
}
