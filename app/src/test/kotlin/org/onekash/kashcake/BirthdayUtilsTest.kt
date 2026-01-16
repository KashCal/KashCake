package org.onekash.kashcake

import org.junit.Assert.assertEquals
import org.junit.Test
import org.onekash.kashcake.domain.BirthdayUtils
import java.time.LocalDate

class BirthdayUtilsTest {

    @Test
    fun `formatDaysUntil returns Today for 0 days`() {
        assertEquals("Today!", BirthdayUtils.formatDaysUntil(0))
    }

    @Test
    fun `formatDaysUntil returns Tomorrow for 1 day`() {
        assertEquals("Tomorrow", BirthdayUtils.formatDaysUntil(1))
    }

    @Test
    fun `formatDaysUntil returns days for less than 7`() {
        assertEquals("In 5 days", BirthdayUtils.formatDaysUntil(5))
    }

    @Test
    fun `formatDaysUntil returns weeks for 7-30 days`() {
        assertEquals("In 2 weeks", BirthdayUtils.formatDaysUntil(14))
        assertEquals("In 1 week", BirthdayUtils.formatDaysUntil(7))
    }

    @Test
    fun `formatDaysUntil returns months for more than 30 days`() {
        assertEquals("In 2 months", BirthdayUtils.formatDaysUntil(60))
        assertEquals("In 1 month", BirthdayUtils.formatDaysUntil(30))
    }

    @Test
    fun `getOrdinalSuffix returns correct suffixes`() {
        assertEquals("1st", BirthdayUtils.getOrdinalSuffix(1))
        assertEquals("2nd", BirthdayUtils.getOrdinalSuffix(2))
        assertEquals("3rd", BirthdayUtils.getOrdinalSuffix(3))
        assertEquals("4th", BirthdayUtils.getOrdinalSuffix(4))
        assertEquals("11th", BirthdayUtils.getOrdinalSuffix(11))
        assertEquals("12th", BirthdayUtils.getOrdinalSuffix(12))
        assertEquals("13th", BirthdayUtils.getOrdinalSuffix(13))
        assertEquals("21st", BirthdayUtils.getOrdinalSuffix(21))
        assertEquals("22nd", BirthdayUtils.getOrdinalSuffix(22))
        assertEquals("23rd", BirthdayUtils.getOrdinalSuffix(23))
        assertEquals("100th", BirthdayUtils.getOrdinalSuffix(100))
    }

    @Test
    fun `adjustForLeapYear handles Feb 29 in non-leap year`() {
        val result = BirthdayUtils.adjustForLeapYear(2, 29, 2023)
        assertEquals(LocalDate.of(2023, 2, 28), result)
    }

    @Test
    fun `adjustForLeapYear keeps Feb 29 in leap year`() {
        val result = BirthdayUtils.adjustForLeapYear(2, 29, 2024)
        assertEquals(LocalDate.of(2024, 2, 29), result)
    }

    @Test
    fun `adjustForLeapYear handles normal dates`() {
        val result = BirthdayUtils.adjustForLeapYear(6, 15, 2023)
        assertEquals(LocalDate.of(2023, 6, 15), result)
    }

    @Test
    fun `formatBirthdayDate without year`() {
        val result = BirthdayUtils.formatBirthdayDate(6, 15)
        assertEquals("June 15", result)
    }

    @Test
    fun `formatBirthdayDate with year`() {
        val result = BirthdayUtils.formatBirthdayDate(6, 15, 1990)
        assertEquals("June 15, 1990", result)
    }

    @Test
    fun `getZodiacSign returns correct signs`() {
        assertEquals("Aries", BirthdayUtils.getZodiacSign(3, 25))
        assertEquals("Taurus", BirthdayUtils.getZodiacSign(4, 25))
        assertEquals("Gemini", BirthdayUtils.getZodiacSign(5, 25))
        assertEquals("Cancer", BirthdayUtils.getZodiacSign(6, 25))
        assertEquals("Leo", BirthdayUtils.getZodiacSign(7, 25))
        assertEquals("Virgo", BirthdayUtils.getZodiacSign(8, 25))
        assertEquals("Libra", BirthdayUtils.getZodiacSign(9, 25))
        assertEquals("Scorpio", BirthdayUtils.getZodiacSign(10, 25))
        assertEquals("Sagittarius", BirthdayUtils.getZodiacSign(11, 25))
        assertEquals("Capricorn", BirthdayUtils.getZodiacSign(12, 25))
        assertEquals("Aquarius", BirthdayUtils.getZodiacSign(1, 25))
        assertEquals("Pisces", BirthdayUtils.getZodiacSign(2, 25))
    }
}
