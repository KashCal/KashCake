package org.onekash.kashcake.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.onekash.kashcake.data.db.entity.Birthday

@Dao
interface BirthdayDao {

    @Query("SELECT * FROM birthdays ORDER BY name ASC")
    fun getAllBirthdays(): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays ORDER BY name ASC")
    suspend fun getAllBirthdaysOnce(): List<Birthday>

    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getBirthdayById(id: Long): Birthday?

    @Query("SELECT * FROM birthdays WHERE id = :id")
    fun getBirthdayByIdFlow(id: Long): Flow<Birthday?>

    @Query("SELECT * FROM birthdays WHERE contact_lookup_key = :lookupKey")
    suspend fun getBirthdayByContactKey(lookupKey: String): Birthday?

    @Query("SELECT * FROM birthdays WHERE contact_lookup_key IS NOT NULL")
    suspend fun getContactLinkedBirthdays(): List<Birthday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(birthday: Birthday): Long

    @Update
    suspend fun update(birthday: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)

    @Query("DELETE FROM birthdays WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM birthdays WHERE contact_lookup_key = :lookupKey")
    suspend fun deleteByContactKey(lookupKey: String)

    @Query("""
        SELECT * FROM birthdays
        WHERE contact_lookup_key NOT IN (:validLookupKeys)
        AND contact_lookup_key IS NOT NULL
    """)
    suspend fun getOrphanedContactBirthdays(validLookupKeys: List<String>): List<Birthday>

    @Query("""
        SELECT * FROM birthdays
        WHERE contact_lookup_key IS NOT NULL
    """)
    suspend fun getAllContactLinkedBirthdaysForOrphanCheck(): List<Birthday>
}
