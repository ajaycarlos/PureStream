package com.purestream.app.di

import android.content.Context
import androidx.room.Room
import com.purestream.app.data.local.AppDatabase
import com.purestream.app.data.local.dao.VideoDao
import com.purestream.app.data.local.dao.ProfanityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "purestream_db"
        )
            .fallbackToDestructiveMigration() // Critical for version 2 update
            .build()
    }

    @Provides
    fun provideVideoDao(db: AppDatabase): VideoDao = db.videoDao()

    @Provides
    fun provideProfanityDao(db: AppDatabase): ProfanityDao = db.profanityDao()
}