package org.onekash.kashcake

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onekash.kashcake.data.contacts.ContactBirthdayImporter
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactBirthdayImporterTest {

    private lateinit var importer: ContactBirthdayImporter

    @Before
    fun setup() {
        importer = ContactBirthdayImporter(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `parseBirthdayDate handles vCard format without year`() {
        val result = importer.parseBirthdayDate("--06-15")
        assertNotNull(result)
        assertEquals(6, result?.first)
        assertEquals(15, result?.second)
        assertNull(result?.third)
    }

    @Test
    fun `parseBirthdayDate handles ISO format`() {
        val result = importer.parseBirthdayDate("1990-06-15")
        assertNotNull(result)
        assertEquals(6, result?.first)
        assertEquals(15, result?.second)
        assertEquals(1990, result?.third)
    }

    @Test
    fun `parseBirthdayDate handles US format`() {
        val result = importer.parseBirthdayDate("6/15/1990")
        assertNotNull(result)
        assertEquals(6, result?.first)
        assertEquals(15, result?.second)
        assertEquals(1990, result?.third)
    }

    @Test
    fun `parseBirthdayDate handles US format with leading zeros`() {
        val result = importer.parseBirthdayDate("06/15/1990")
        assertNotNull(result)
        assertEquals(6, result?.first)
        assertEquals(15, result?.second)
        assertEquals(1990, result?.third)
    }

    @Test
    fun `parseBirthdayDate rejects invalid month`() {
        val result = importer.parseBirthdayDate("13/15/1990")
        assertNull(result)
    }

    @Test
    fun `parseBirthdayDate rejects invalid day`() {
        val result = importer.parseBirthdayDate("6/32/1990")
        assertNull(result)
    }

    @Test
    fun `parseBirthdayDate rejects invalid format`() {
        val result = importer.parseBirthdayDate("June 15, 1990")
        assertNull(result)
    }

    @Test
    fun `parseBirthdayDate rejects very old year`() {
        val result = importer.parseBirthdayDate("1800-06-15")
        assertNull(result)
    }
}
