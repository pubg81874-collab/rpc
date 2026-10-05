package com.example.data.repository

import com.example.data.local.RpcDao
import com.example.data.local.RpcPresetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RpcRepository(private val rpcDao: RpcDao) {
  val allPresets: Flow<List<RpcPresetEntity>> = rpcDao.getAllPresets()

  suspend fun getPresetById(id: Long): RpcPresetEntity? = withContext(Dispatchers.IO) {
    rpcDao.getPresetById(id)
  }

  suspend fun insertPreset(preset: RpcPresetEntity): Long = withContext(Dispatchers.IO) {
    rpcDao.insertPreset(preset)
  }

  suspend fun updatePreset(preset: RpcPresetEntity) = withContext(Dispatchers.IO) {
    rpcDao.updatePreset(preset)
  }

  suspend fun deletePreset(preset: RpcPresetEntity) = withContext(Dispatchers.IO) {
    rpcDao.deletePreset(preset)
  }

  suspend fun deletePresetById(id: Long) = withContext(Dispatchers.IO) {
    rpcDao.deletePresetById(id)
  }

  suspend fun ensureDefaultPresets() = withContext(Dispatchers.IO) {
    if (rpcDao.getCount() == 0) {
      val defaultPresets = listOf(
        RpcPresetEntity(
          id = 1,
          presetName = "The Mentalist - S2E11",
          mediaName = "The Mentalist",
          title = "Rose-Colored Glasses",
          subtitle = "Rigsby must go undercover as an alumnus to inve...",
          mediaType = "TV_SHOW",
          posterUri = null,
          posterResName = "poster_detective",
          isPlaying = false,
          badgeType = "PAUSE",
          currentTimeSeconds = 581L,
          totalDurationSeconds = 2580L,
          remainingTimeText = "9:41",
          seasonEpisodeText = "S2E11",
          button1Text = "Watch Episode",
          button1Url = "https://www.netflix.com",
          button2Text = "View Series",
          button2Url = "https://www.imdb.com/title/tt1196946/",
          isFavorite = true,
          createdAt = System.currentTimeMillis() - 10000
        ),
        RpcPresetEntity(
          id = 2,
          presetName = "Modha Rathri (2026)",
          mediaName = "Modha Rathri",
          title = "Modha Rathri",
          subtitle = "2026   141 minutes",
          mediaType = "MOVIE",
          posterUri = null,
          posterResName = "poster_modha_rathri",
          isPlaying = true,
          badgeType = "PLAY",
          currentTimeSeconds = 3829L,
          totalDurationSeconds = 8515L,
          remainingTimeText = "01:18:06",
          seasonEpisodeText = "Movie",
          button1Text = "Watch Movie",
          button1Url = "https://www.primevideo.com",
          button2Text = "",
          button2Url = "",
          isFavorite = true,
          createdAt = System.currentTimeMillis() - 20000
        )
      )
      rpcDao.insertPresets(defaultPresets)
    }
  }
}
