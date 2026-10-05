package com.example.data.model

data class DiscordUser(
  val id: String = "",
  val username: String = "",
  val globalName: String? = null,
  val avatarUrl: String? = null,
  val accentColor: Long? = null,
  val token: String = "",
  val isLoggedIn: Boolean = false
) {
  val displayName: String
    get() = globalName?.ifBlank { username } ?: username.ifBlank { "Discord User" }

  val tag: String
    get() = "@$username"
}
