package com.purestream.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purestream.app.data.local.dao.ProfanityDao
import com.purestream.app.data.local.entity.ProfanityEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profanityDao: ProfanityDao
) : ViewModel() {

    val blacklist = profanityDao.getAllWords()

    fun addWord(word: String) {
        if (word.isBlank()) return
        viewModelScope.launch {
            profanityDao.insertWord(ProfanityEntity(word = word.lowercase().trim()))
        }
    }

    fun removeWord(word: ProfanityEntity) {
        viewModelScope.launch {
            profanityDao.deleteWord(word)
        }
    }
}