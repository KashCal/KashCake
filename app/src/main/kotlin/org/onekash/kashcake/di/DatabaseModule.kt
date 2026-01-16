package org.onekash.kashcake.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.onekash.kashcake.data.db.KashCakeDatabase
import org.onekash.kashcake.data.db.dao.BirthdayDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KashCakeDatabase {
        return Room.databaseBuilder(
            context,
            KashCakeDatabase::class.java,
            KashCakeDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideBirthdayDao(database: KashCakeDatabase): BirthdayDao {
        return database.birthdayDao()
    }
}
