package com.purestream.app.data.local.dao

import androidx.room.*
import com.purestream.app.data.local.entity.ProfanityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfanityDao {
    @Query("SELECT * FROM profanity_words ORDER BY word ASC")
    fun getAllWords(): Flow<List<ProfanityEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: ProfanityEntity)

    @Delete
    suspend fun deleteWord(word: ProfanityEntity)
}