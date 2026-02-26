package com.purestream.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.purestream.app.data.local.dao.ProfanityDao
import com.purestream.app.data.local.dao.VideoDao
import com.purestream.app.data.local.entity.ProfanityEntity
import com.purestream.app.data.local.entity.VideoEntity

@Database(entities = [VideoEntity::class, ProfanityEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun profanityDao(): ProfanityDao
}