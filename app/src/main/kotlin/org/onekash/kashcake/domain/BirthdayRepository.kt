package org.onekash.kashcake.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.onekash.kashcake.data.db.dao.BirthdayDao
import org.onekash.kashcake.data.db.entity.Birthday
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BirthdayRepository @Inject constructor(
    private val birthdayDao: BirthdayDao
) {
    fun getAllBirthdays(): Flow<List<Birthday>> = birthdayDao.getAllBirthdays()

    fun getUpcomingBirthdays(): Flow<List<Birthday>> {
        return birthdayDao.getAllBirthdays().map { birthdays ->
            birthdays.sortedBy { it.daysUntilBirthday() }
        }
    }

    fun getTodaysBirthdays(): Flow<List<Birthday>> {
        return birthdayDao.getAllBirthdays().map { birthdays ->
            birthdays.filter { it.isBirthdayToday() }
        }
    }

    suspend fun getBirthdayById(id: Long): Birthday? = birthdayDao.getBirthdayById(id)
}
