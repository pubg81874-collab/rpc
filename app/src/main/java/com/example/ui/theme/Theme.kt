package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DiscordDarkColorScheme = darkColorScheme(
  primary = DiscordBlurple,
  onPrimary = DiscordTextPrimary,
  primaryContainer = DiscordCardBg,
  onPrimaryContainer = DiscordTextPrimary,
  secondary = DiscordGreen,
  onSecondary = DiscordDarkBg,
  background = DiscordDarkBg,
  onBackground = DiscordTextPrimary,
  surface = DiscordCardBg,
  onSurface = DiscordTextPrimary,
  surfaceVariant = DiscordItemBg,
  onSurfaceVariant = DiscordTextSecondary,
  outline = DiscordBorder
)

private val DiscordLightColorScheme = lightColorScheme(
  primary = DiscordBlurple,
  onPrimary = DiscordTextPrimary,
  background = Color(0xFFF2F3F5),
  onBackground = Color(0xFF2B2D31),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF2B2D31),
  outline = Color(0xFFE3E5E8)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to Discord dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DiscordDarkColorScheme else DiscordLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
