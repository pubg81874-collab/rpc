package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.BadgeType
import com.example.data.model.MediaType
import com.example.data.model.PreMiDCardTheme
import com.example.data.model.RpcActivity
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBorder
import com.example.ui.theme.DiscordButton
import com.example.ui.theme.DiscordCardBg
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordItemBg
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.DiscordTextSecondary

@Composable
fun DiscordRpcCard(
  activity: RpcActivity,
  modifier: Modifier = Modifier,
  onTogglePlayPause: (() -> Unit)? = null,
  onButtonClick: ((String) -> Unit)? = null
) {
  val context = LocalContext.current
  val themeBgColor = Color(activity.cardTheme.primaryColorHex)
  val themeGlowColor = Color(activity.cardTheme.glowColorHex)
  val hasCustomGlow = activity.cardTheme != PreMiDCardTheme.DISCORD_DARK

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .then(
        if (hasCustomGlow) {
          Modifier.border(1.5.dp, themeGlowColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        } else {
          Modifier
        }
      )
      .testTag("discord_rpc_card"),
    shape = RoundedCornerShape(16.dp),
    color = themeBgColor,
    shadowElevation = if (hasCustomGlow) 8.dp else 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // PreMiD Pro Service Tag & Status Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // PreMiD Service Pill
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(themeGlowColor.copy(alpha = 0.18f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "PreMiD Pro",
                tint = if (activity.cardTheme == PreMiDCardTheme.VIP_GOLD) Color(0xFFFFD700) else themeGlowColor,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "PreMiD • ${activity.serviceName}",
                color = if (activity.cardTheme == PreMiDCardTheme.VIP_GOLD) Color(0xFFFFD700) else themeGlowColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          // Live Stream Pill
          if (activity.isLiveStream) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFED4245).copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFED4245))
                )
                Text(
                  text = if (activity.formattedViewerCount.isNotBlank()) "LIVE (${activity.formattedViewerCount})" else "LIVE",
                  color = Color(0xFFED4245),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
          // Party / Co-Op Pill
          if (activity.partyCurrent != null && activity.partyMax != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(DiscordItemBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Party",
                tint = DiscordTextSecondary,
                modifier = Modifier.size(11.dp)
              )
              Text(
                text = "${activity.partyCurrent}/${activity.partyMax}",
                color = DiscordTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // User Discord Status Indicator
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          val statusColor = when (activity.userStatus) {
            com.example.data.model.DiscordUserStatus.ONLINE -> DiscordGreen
            com.example.data.model.DiscordUserStatus.IDLE -> com.example.ui.theme.DiscordYellow
            com.example.data.model.DiscordUserStatus.DND -> com.example.ui.theme.DiscordRed
            com.example.data.model.DiscordUserStatus.INVISIBLE -> DiscordTextMuted
          }
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(statusColor)
          )
          Text(
            text = activity.userStatus.label,
            color = DiscordTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Header: Activity prefix + Media Name
      Text(
        text = activity.headerText,
        color = DiscordTextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      )

      // Media details row: Poster + Info
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Poster Box with badge overlay
        Box(
          modifier = Modifier
            .size(76.dp)
            .testTag("rpc_poster_box")
        ) {
          // Poster image
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E1F22))
          ) {
            when {
              activity.posterResId != null -> {
                Image(
                  painter = painterResource(id = activity.posterResId),
                  contentDescription = "Poster artwork",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
              !activity.posterUri.isNullOrBlank() -> {
                AsyncImage(
                  model = ImageRequest.Builder(context)
                    .data(activity.posterUri)
                    .crossfade(true)
                    .build(),
                  contentDescription = "Poster artwork",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop,
                  error = painterResource(id = R.drawable.poster_detective),
                  placeholder = painterResource(id = R.drawable.poster_detective)
                )
              }
              else -> {
                // Fallback default poster
                Image(
                  painter = painterResource(id = R.drawable.poster_detective),
                  contentDescription = "Default poster",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
            }

            // GIF badge if animated
            val isAnimatedGif = activity.isGif || activity.posterUri?.contains(".gif", ignoreCase = true) == true
            if (isAnimatedGif) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(4.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(Color(0xCC000000))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "GIF",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Badge Overlay on bottom-right of poster
          if (activity.badgeType != BadgeType.NONE) {
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF111214))
                .border(1.5.dp, Color(0xFF2B2D31), CircleShape)
                .clickable { onTogglePlayPause?.invoke() }
                .testTag("rpc_play_pause_badge"),
              contentAlignment = Alignment.Center
            ) {
              if (activity.badgeType == BadgeType.PAUSE) {
                // Pause icon: two vertical bars
                Icon(
                  imageVector = Icons.Default.Pause,
                  contentDescription = "Paused",
                  tint = Color.White,
                  modifier = Modifier.size(14.dp)
                )
              } else {
                // Play icon: white triangle
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Playing",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title, Subtitle, and Badges
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          // Episode / Movie Title
          Text(
            text = activity.title.ifBlank { "Untitled" },
            color = DiscordTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          // Subtitle / Plot Summary
          Text(
            text = activity.subtitle.ifBlank { "Streaming on Discord" },
            color = DiscordTextSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(6.dp))

          // If TV Show mode or badges are visible:
          if (activity.mediaType == MediaType.TV_SHOW) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // Remaining / Elapsed Time Badge (Green icon + text)
              if (activity.remainingTimeText.isNotBlank()) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Remaining time",
                    tint = DiscordGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = activity.remainingTimeText,
                    color = DiscordGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              // Season and Episode Badge (S2E11)
              if (activity.seasonEpisodeText.isNotBlank()) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Season and Episode",
                    tint = DiscordTextSecondary,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = activity.seasonEpisodeText,
                    color = DiscordTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }

      // If Movie mode: Custom Discord Progress Bar + Timestamps (Screenshot 2)
      if (activity.mediaType == MediaType.MOVIE) {
        Spacer(modifier = Modifier.height(14.dp))
        // Progress bar container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(2.5.dp))
            .background(Color(0xFF3F4147))
            .testTag("rpc_progress_bar")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction = activity.progressFraction)
              .fillMaxHeight()
              .background(DiscordBlurple)
          )
        }
        Spacer(modifier = Modifier.height(5.dp))
        // Timestamps Row: Left = current time, Right = total duration
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = activity.formatCurrentTime(),
            color = DiscordTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.2).sp
          )
          Text(
            text = activity.formatTotalDuration(),
            color = DiscordTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.2).sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Button 1: "Watch Episode" or "Watch Movie"
      if (activity.button1Text.isNotBlank()) {
        DiscordActionButton(
          label = activity.button1Text,
          onClick = {
            if (activity.button1Url.isNotBlank()) {
              runCatching {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.button1Url))
                context.startActivity(intent)
              }
            }
            onButtonClick?.invoke(activity.button1Text)
          },
          testTag = "rpc_action_button_1"
        )
      }

      // Action Button 2: "View Series" (if configured)
      if (activity.button2Text.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        DiscordActionButton(
          label = activity.button2Text,
          onClick = {
            if (activity.button2Url.isNotBlank()) {
              runCatching {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.button2Url))
                context.startActivity(intent)
              }
            }
            onButtonClick?.invoke(activity.button2Text)
          },
          testTag = "rpc_action_button_2"
        )
      }
    }
  }
}

@Composable
fun DiscordActionButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "discord_action_button"
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(6.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = DiscordButton,
      contentColor = DiscordTextPrimary
    )
  ) {
    Text(
      text = label,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = DiscordTextPrimary
    )
  }
}
