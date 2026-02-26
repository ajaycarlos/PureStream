package com.purestream.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profanity_words")
data class ProfanityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String
)