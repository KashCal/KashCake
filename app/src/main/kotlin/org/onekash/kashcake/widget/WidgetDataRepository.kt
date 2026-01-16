package org.onekash.kashcake.widget

import kotlinx.coroutines.flow.first
import org.onekash.kashcake.data.db.dao.BirthdayDao

class WidgetDataRepository(private val birthdayDao: BirthdayDao) {

    data class WidgetBirthday(
        val id: Long,
        val name: String,
        val daysUntil: Long,
        val isToday: Boolean
    ) {
        val daysUntilText: String
            get() = when {
                isToday -> "Today!"
                daysUntil == 1L -> "Tomorrow"
                else -> "in $daysUntil days"
            }
    }

    suspend fun getUpcomingBirthdays(limit: Int): List<WidgetBirthday> {
        return birthdayDao.getAllBirthdays()
            .first()
            .map { birthday ->
                WidgetBirthday(
                    id = birthday.id,
                    name = birthday.name,
                    daysUntil = birthday.daysUntilBirthday(),
                    isToday = birthday.isBirthdayToday()
                )
            }
            .sortedBy { it.daysUntil }
            .take(limit)
    }
}
