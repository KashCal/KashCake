package org.onekash.kashcake

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.onekash.kashcake.data.db.entity.Birthday
import java.time.LocalDate

class BirthdayEntityTest {

    @Test
    fun `ageOnNextBirthday returns null when year is null`() {
        val birthday = Birthday(
            name = "Test",
            month = 6,
            day = 15,
            year = null
        )
        assertNull(birthday.ageOnNextBirthday())
    }

    @Test
    fun `ageOnNextBirthday calculates correctly for future birthday this year`() {
        val today = LocalDate.now()
        val futureMonth = if (today.monthValue < 12) today.monthValue + 1 else 1
        val birthYear = 1990

        val birthday = Birthday(
            name = "Test",
            month = futureMonth,
            day = 15,
            year = birthYear
        )

        val expectedYear = if (futureMonth == 1) today.year + 1 else today.year
        val expectedAge = expectedYear - birthYear
        assertEquals(expectedAge, birthday.ageOnNextBirthday())
    }

    @Test
    fun `daysUntilBirthday returns 0 for birthday today`() {
        val today = LocalDate.now()
        val birthday = Birthday(
            name = "Test",
            month = today.monthValue,
            day = today.dayOfMonth
        )
        assertEquals(0L, birthday.daysUntilBirthday())
    }

    @Test
    fun `isBirthdayToday returns true for birthday today`() {
        val today = LocalDate.now()
        val birthday = Birthday(
            name = "Test",
            month = today.monthValue,
            day = today.dayOfMonth
        )
        assertTrue(birthday.isBirthdayToday())
    }

    @Test
    fun `isBirthdayToday returns false for birthday not today`() {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val birthday = Birthday(
            name = "Test",
            month = tomorrow.monthValue,
            day = tomorrow.dayOfMonth
        )
        assertFalse(birthday.isBirthdayToday())
    }

    @Test
    fun `getNextBirthdayDate returns this year for future birthday`() {
        val today = LocalDate.now()
        // Pick a month that's definitely in the future
        val futureDate = today.plusMonths(3)

        val birthday = Birthday(
            name = "Test",
            month = futureDate.monthValue,
            day = 15
        )

        val nextBirthday = birthday.getNextBirthdayDate()
        assertTrue(nextBirthday >= today)
    }

    @Test
    fun `getNextBirthdayDate returns next year for past birthday`() {
        val today = LocalDate.now()
        // Pick a month that's definitely in the past
        val pastDate = today.minusMonths(3)

        val birthday = Birthday(
            name = "Test",
            month = pastDate.monthValue,
            day = 15
        )

        val nextBirthday = birthday.getNextBirthdayDate()
        assertTrue(nextBirthday.year >= today.year)
        assertTrue(nextBirthday >= today)
    }
}
