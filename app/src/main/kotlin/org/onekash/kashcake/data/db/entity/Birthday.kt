package org.onekash.kashcake.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Entity(
    tableName = "birthdays",
    indices = [Index(value = ["contact_lookup_key"], unique = true)]
)
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val month: Int,  // 1-12

    val day: Int,    // 1-31

    @ColumnInfo(name = "year")
    val year: Int? = null,  // Optional birth year

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,

    @ColumnInfo(name = "contact_lookup_key")
    val contactLookupKey: String? = null,  // Link to Android contact

    @ColumnInfo(name = "reminder_days_before")
    val reminderDaysBefore: Int = 1,  // Default: 1 day before

    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate age on next birthday (if year is known)
     */
    fun ageOnNextBirthday(): Int? {
        if (year == null) return null
        val nextBirthday = getNextBirthdayDate()
        return nextBirthday.year - year
    }

    /**
     * Calculate age they turned this year (if year is known)
     * Used for past birthdays in current month
     */
    fun ageThisYear(): Int? {
        if (year == null) return null
        return LocalDate.now().year - year
    }

    /**
     * Get next birthday date
     */
    fun getNextBirthdayDate(): LocalDate {
        val today = LocalDate.now()
        val birthdayThisYear = LocalDate.of(
            today.year,
            month,
            day.coerceAtMost(YearMonth.of(today.year, month).lengthOfMonth())
        )
        return if (birthdayThisYear >= today) {
            birthdayThisYear
        } else {
            LocalDate.of(
                today.year + 1,
                month,
                day.coerceAtMost(YearMonth.of(today.year + 1, month).lengthOfMonth())
            )
        }
    }

    /**
     * Days until next birthday
     */
    fun daysUntilBirthday(): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), getNextBirthdayDate())
    }

    /**
     * Is birthday today?
     */
    fun isBirthdayToday(): Boolean {
        val today = LocalDate.now()
        return today.monthValue == month && today.dayOfMonth == day
    }
}
