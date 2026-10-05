package com.example.data.model

import com.example.R

enum class MediaType(val label: String) {
  TV_SHOW("TV Show"),
  MOVIE("Movie")
}

enum class BadgeType(val label: String) {
  PLAY("Playing"),
  PAUSE("Paused"),
  NONE("None")
}

enum class DiscordActivityType(val code: Int, val label: String, val prefix: String) {
  WATCHING(3, "Watching", "Watching"),
  PLAYING(0, "Playing", "Playing"),
  STREAMING(1, "Streaming", "Streaming"),
  LISTENING(2, "Listening", "Listening to"),
  COMPETING(5, "Competing", "Competing in")
}

enum class DiscordUserStatus(val code: String, val label: String) {
  ONLINE("online", "Online"),
  IDLE("idle", "Idle"),
  DND("dnd", "Do Not Disturb"),
  INVISIBLE("invisible", "Invisible")
}

enum class PreMiDCardTheme(val label: String, val primaryColorHex: Long, val glowColorHex: Long) {
  DISCORD_DARK("Classic Dark", 0xFF2B2D31, 0xFF5865F2),
  OLED_BLACK("Midnight OLED", 0xFF0B0C0E, 0xFF8A5CF6),
  PREMID_GLOW("PreMiD Cyberpunk Glow", 0xFF121D24, 0xFF00E5FF),
  NITRO_BURST("Nitro Fuchsia", 0xFF220D1D, 0xFFEB459E),
  VIP_GOLD("PreMiD VIP Gold", 0xFF241D0D, 0xFFFFD700)
}

data class RpcActivity(
  val id: Long = 0,
  val mediaName: String = "The Mentalist",
  val title: String = "Rose-Colored Glasses",
  val subtitle: String = "Rigsby must go undercover as an alumnus to inve...",
  val mediaType: MediaType = MediaType.TV_SHOW,
  val activityType: DiscordActivityType = DiscordActivityType.WATCHING,
  val userStatus: DiscordUserStatus = DiscordUserStatus.ONLINE,
  val posterUri: String? = null,
  val posterResId: Int? = R.drawable.poster_detective,
  val isPlaying: Boolean = false,
  val badgeType: BadgeType = BadgeType.PAUSE,
  val currentTimeSeconds: Long = 3829L, // 01:03:49
  val totalDurationSeconds: Long = 8515L, // 02:21:55
  val remainingTimeText: String = "9:41",
  val seasonEpisodeText: String = "S2E11",
  val button1Text: String = "Watch Episode",
  val button1Url: String = "https://www.netflix.com",
  val button2Text: String = "View Series",
  val button2Url: String = "https://www.imdb.com",
  val applicationId: String = "109876543210987654",
  val isGif: Boolean = false,
  // PreMiD Premium features
  val serviceName: String = "Netflix",
  val partyCurrent: Int? = null,
  val partyMax: Int? = null,
  val isLiveStream: Boolean = false,
  val viewerCount: Int? = null,
  val cardTheme: PreMiDCardTheme = PreMiDCardTheme.DISCORD_DARK
) {
  fun formatCurrentTime(): String = formatSeconds(currentTimeSeconds)
  fun formatTotalDuration(): String = formatSeconds(totalDurationSeconds)

  val headerText: String
    get() = "${activityType.prefix} ${mediaName.ifBlank { "Media" }}"

  val progressFraction: Float
    get() = if (totalDurationSeconds > 0) {
      (currentTimeSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

  val partyText: String?
    get() = if (partyCurrent != null && partyMax != null && partyMax > 0) {
      "Party ($partyCurrent of $partyMax)"
    } else null

  val formattedViewerCount: String
    get() = when {
      viewerCount == null -> ""
      viewerCount >= 1_000_000 -> String.format("%.1fM", viewerCount / 1_000_000f)
      viewerCount >= 1_000 -> String.format("%.1fK", viewerCount / 1_000f)
      else -> viewerCount.toString()
    }

  companion object {
    fun formatSeconds(totalSecs: Long): String {
      val hours = totalSecs / 3600
      val minutes = (totalSecs % 3600) / 60
      val seconds = totalSecs % 60
      return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
      } else {
        String.format("%02d:%02d", minutes, seconds)
      }
    }
  }
}
