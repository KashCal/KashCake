package org.onekash.kashcake.domain

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

object BirthdayUtils {

    /**
     * Format birthday date as "January 15" or "January 15, 1990"
     */
    fun formatBirthdayDate(month: Int, day: Int, year: Int? = null): String {
        val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
        return if (year != null) {
            "$monthName $day, $year"
        } else {
            "$monthName $day"
        }
    }

    /**
     * Format days until as human-readable text
     */
    fun formatDaysUntil(days: Long): String {
        return when {
            days == 0L -> "Today!"
            days == 1L -> "Tomorrow"
            days < 7L -> "In $days days"
            days < 30L -> {
                val weeks = days / 7
                if (weeks == 1L) "In 1 week" else "In $weeks weeks"
            }
            else -> {
                val months = days / 30
                if (months == 1L) "In 1 month" else "In $months months"
            }
        }
    }

    /**
     * Handle Feb 29 birthdays in non-leap years
     */
    fun adjustForLeapYear(month: Int, day: Int, year: Int): LocalDate {
        return try {
            LocalDate.of(year, month, day)
        } catch (e: DateTimeException) {
            // Feb 29 in non-leap year -> Feb 28
            if (month == 2 && day == 29) {
                LocalDate.of(year, 2, 28)
            } else {
                throw e
            }
        }
    }

    /**
     * Get ordinal suffix (1st, 2nd, 3rd, etc.)
     */
    fun getOrdinalSuffix(number: Int): String {
        return when {
            number % 100 in 11..13 -> "${number}th"
            number % 10 == 1 -> "${number}st"
            number % 10 == 2 -> "${number}nd"
            number % 10 == 3 -> "${number}rd"
            else -> "${number}th"
        }
    }

    /**
     * Get zodiac sign for a birthday
     */
    fun getZodiacSign(month: Int, day: Int): String {
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Aries"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Taurus"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Gemini"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Cancer"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Leo"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Virgo"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Libra"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Scorpio"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Sagittarius"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Capricorn"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Aquarius"
            else -> "Pisces"
        }
    }
}
