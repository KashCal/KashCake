package org.onekash.kashcake.data.contacts

import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactBirthdayImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class ContactBirthday(
        val lookupKey: String,
        val displayName: String,
        val month: Int,
        val day: Int,
        val year: Int?,
        val photoUri: String?
    )

    fun getContactsWithBirthdays(): List<ContactBirthday> {
        val birthdays = mutableListOf<ContactBirthday>()

        val projection = arrayOf(
            ContactsContract.Data.LOOKUP_KEY,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.Contacts.PHOTO_URI
        )

        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND " +
            "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"

        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        try {
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                ContactsContract.Data.DISPLAY_NAME
            )?.use { cursor ->
                val lookupKeyIndex = cursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY)
                val displayNameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                val birthdayIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                val photoUriIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

                while (cursor.moveToNext()) {
                    val lookupKey = cursor.getString(lookupKeyIndex) ?: continue
                    val displayName = cursor.getString(displayNameIndex) ?: continue
                    val birthdayString = cursor.getString(birthdayIndex) ?: continue
                    val photoUri = cursor.getString(photoUriIndex)

                    parseBirthdayDate(birthdayString)?.let { (month, day, year) ->
                        birthdays.add(
                            ContactBirthday(
                                lookupKey = lookupKey,
                                displayName = displayName,
                                month = month,
                                day = day,
                                year = year,
                                photoUri = photoUri
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
            return emptyList()
        }

        // Deduplicate by (name, month, day) - same person may appear multiple times
        // when contact is saved to both local storage and Google/cloud accounts
        return birthdays.distinctBy { Triple(it.displayName, it.month, it.day) }
    }

    /**
     * Parse birthday date from various formats:
     * - "--MM-DD" (vCard, no year)
     * - "YYYY-MM-DD" (ISO)
     * - "MM/DD/YYYY" (US)
     * - "DD/MM/YYYY" (European)
     */
    internal fun parseBirthdayDate(dateString: String): Triple<Int, Int, Int?>? {
        return when {
            // vCard format without year: --MM-DD
            dateString.startsWith("--") -> {
                val parts = dateString.removePrefix("--").split("-")
                if (parts.size == 2) {
                    val month = parts[0].toIntOrNull() ?: return null
                    val day = parts[1].toIntOrNull() ?: return null
                    if (month in 1..12 && day in 1..31) {
                        Triple(month, day, null)
                    } else null
                } else null
            }
            // ISO format: YYYY-MM-DD
            dateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                val parts = dateString.split("-")
                val year = parts[0].toIntOrNull() ?: return null
                val month = parts[1].toIntOrNull() ?: return null
                val day = parts[2].toIntOrNull() ?: return null
                if (month in 1..12 && day in 1..31 && year > 1900) {
                    Triple(month, day, year)
                } else null
            }
            // US format: MM/DD/YYYY or M/D/YYYY
            dateString.matches(Regex("\\d{1,2}/\\d{1,2}/\\d{4}")) -> {
                val parts = dateString.split("/")
                val month = parts[0].toIntOrNull() ?: return null
                val day = parts[1].toIntOrNull() ?: return null
                val year = parts[2].toIntOrNull() ?: return null
                if (month in 1..12 && day in 1..31 && year > 1900) {
                    Triple(month, day, year)
                } else null
            }
            else -> null
        }
    }
}
