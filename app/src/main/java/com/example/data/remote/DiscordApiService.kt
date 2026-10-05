package com.example.data.remote

import com.example.data.model.DiscordUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DiscordApiService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  suspend fun fetchCurrentUser(token: String): Result<DiscordUser> = withContext(Dispatchers.IO) {
    try {
      val trimmedToken = token.trim()
      if (trimmedToken.isBlank()) {
        return@withContext Result.failure(IllegalArgumentException("Token cannot be blank"))
      }

      val request = Request.Builder()
        .url("https://discord.com/api/v10/users/@me")
        .header("Authorization", trimmedToken)
        .header("User-Agent", "Discord-Android-RPC/1.0")
        .build()

      val response = client.newCall(request).execute()
      val body = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        return@withContext Result.failure(
          Exception(
            if (response.code == 401) "Invalid Discord Token (401 Unauthorized)"
            else "Discord API error: ${response.code}"
          )
        )
      }

      val json = JSONObject(body)
      val id = json.optString("id", "")
      val username = json.optString("username", "User")
      val globalName = if (json.has("global_name") && !json.isNull("global_name")) {
        json.getString("global_name")
      } else null

      val avatarHash = if (json.has("avatar") && !json.isNull("avatar")) {
        json.getString("avatar")
      } else null

      val avatarUrl = if (!avatarHash.isNullOrBlank() && id.isNotBlank()) {
        "https://cdn.discordapp.com/avatars/$id/$avatarHash.png?size=256"
      } else {
        val defaultIdx = try { (id.toLong() shr 22) % 6 } catch (e: Exception) { 0L }
        "https://cdn.discordapp.com/embed/avatars/$defaultIdx.png"
      }

      val accentColor = if (json.has("accent_color") && !json.isNull("accent_color")) {
        json.optLong("accent_color")
      } else null

      val user = DiscordUser(
        id = id,
        username = username,
        globalName = globalName,
        avatarUrl = avatarUrl,
        accentColor = accentColor,
        token = trimmedToken,
        isLoggedIn = true
      )
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
