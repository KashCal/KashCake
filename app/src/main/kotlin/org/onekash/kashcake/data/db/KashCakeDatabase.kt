package org.onekash.kashcake.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.onekash.kashcake.data.db.dao.BirthdayDao
import org.onekash.kashcake.data.db.entity.Birthday

@Database(
    entities = [Birthday::class],
    version = 1,
    exportSchema = false
)
abstract class KashCakeDatabase : RoomDatabase() {

    abstract fun birthdayDao(): BirthdayDao

    companion object {
        const val DATABASE_NAME = "kashcake.db"

        @Volatile
        private var INSTANCE: KashCakeDatabase? = null

        fun getInstance(context: Context): KashCakeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KashCakeDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
