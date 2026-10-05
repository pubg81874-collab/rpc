package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BadgeType
import com.example.data.model.DiscordActivityType
import com.example.data.model.DiscordUserStatus
import com.example.data.model.MediaType
import com.example.data.model.PreMiDCardTheme
import com.example.data.model.RpcActivity

@Entity(tableName = "rpc_presets")
data class RpcPresetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val presetName: String,
  val mediaName: String,
  val title: String,
  val subtitle: String,
  val mediaType: String, // "TV_SHOW" or "MOVIE"
  val activityType: String = "WATCHING",
  val userStatus: String = "ONLINE",
  val posterUri: String?,
  val posterResName: String?,
  val isPlaying: Boolean,
  val badgeType: String, // "PLAY", "PAUSE", "NONE"
  val currentTimeSeconds: Long,
  val totalDurationSeconds: Long,
  val remainingTimeText: String,
  val seasonEpisodeText: String,
  val button1Text: String,
  val button1Url: String,
  val button2Text: String,
  val button2Url: String,
  val isFavorite: Boolean = false,
  val isGif: Boolean = false,
  // PreMiD Premium fields
  val serviceName: String = "Netflix",
  val partyCurrent: Int? = null,
  val partyMax: Int? = null,
  val isLiveStream: Boolean = false,
  val viewerCount: Int? = null,
  val cardTheme: String = "DISCORD_DARK",
  val createdAt: Long = System.currentTimeMillis()
) {
  fun toRpcActivity(): RpcActivity {
    return RpcActivity(
      id = id,
      mediaName = mediaName,
      title = title,
      subtitle = subtitle,
      mediaType = try { MediaType.valueOf(mediaType) } catch (e: Exception) { MediaType.TV_SHOW },
      activityType = try { DiscordActivityType.valueOf(activityType) } catch (e: Exception) { DiscordActivityType.WATCHING },
      userStatus = try { DiscordUserStatus.valueOf(userStatus) } catch (e: Exception) { DiscordUserStatus.ONLINE },
      posterUri = posterUri,
      posterResId = when (posterResName) {
        "poster_detective" -> com.example.R.drawable.poster_detective
        "poster_modha_rathri" -> com.example.R.drawable.poster_modha_rathri
        else -> null
      },
      isPlaying = isPlaying,
      badgeType = try { BadgeType.valueOf(badgeType) } catch (e: Exception) { BadgeType.PAUSE },
      currentTimeSeconds = currentTimeSeconds,
      totalDurationSeconds = totalDurationSeconds,
      remainingTimeText = remainingTimeText,
      seasonEpisodeText = seasonEpisodeText,
      button1Text = button1Text,
      button1Url = button1Url,
      button2Text = button2Text,
      button2Url = button2Url,
      isGif = isGif,
      serviceName = serviceName,
      partyCurrent = partyCurrent,
      partyMax = partyMax,
      isLiveStream = isLiveStream,
      viewerCount = viewerCount,
      cardTheme = try { PreMiDCardTheme.valueOf(cardTheme) } catch (e: Exception) { PreMiDCardTheme.DISCORD_DARK }
    )
  }

  companion object {
    fun fromRpcActivity(presetName: String, activity: RpcActivity, isFavorite: Boolean = false): RpcPresetEntity {
      val resName = when (activity.posterResId) {
        com.example.R.drawable.poster_detective -> "poster_detective"
        com.example.R.drawable.poster_modha_rathri -> "poster_modha_rathri"
        else -> null
      }
      return RpcPresetEntity(
        id = activity.id,
        presetName = presetName,
        mediaName = activity.mediaName,
        title = activity.title,
        subtitle = activity.subtitle,
        mediaType = activity.mediaType.name,
        activityType = activity.activityType.name,
        userStatus = activity.userStatus.name,
        posterUri = activity.posterUri,
        posterResName = resName,
        isPlaying = activity.isPlaying,
        badgeType = activity.badgeType.name,
        currentTimeSeconds = activity.currentTimeSeconds,
        totalDurationSeconds = activity.totalDurationSeconds,
        remainingTimeText = activity.remainingTimeText,
        seasonEpisodeText = activity.seasonEpisodeText,
        button1Text = activity.button1Text,
        button1Url = activity.button1Url,
        button2Text = activity.button2Text,
        button2Url = activity.button2Url,
        isFavorite = isFavorite,
        isGif = activity.isGif,
        serviceName = activity.serviceName,
        partyCurrent = activity.partyCurrent,
        partyMax = activity.partyMax,
        isLiveStream = activity.isLiveStream,
        viewerCount = activity.viewerCount,
        cardTheme = activity.cardTheme.name
      )
    }
  }
}
