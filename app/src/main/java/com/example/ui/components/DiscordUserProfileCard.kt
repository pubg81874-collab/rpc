package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.DiscordUser
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordCardBg
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordItemBg
import com.example.ui.theme.DiscordRed
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.DiscordTextSecondary

@Composable
fun DiscordUserProfileCard(
  user: DiscordUser,
  onLogout: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val bannerColor = if (user.accentColor != null) {
    Color(user.accentColor.toInt() or (0xFF shl 24))
  } else {
    DiscordBlurple
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
    modifier = modifier
      .fillMaxWidth()
      .testTag("discord_user_profile_card")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Banner Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(72.dp)
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(bannerColor, bannerColor.copy(alpha = 0.7f))
            )
          )
      ) {
        IconButton(
          onClick = onRefresh,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh Profile",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Avatar & Details Content with upward negative offset for avatar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(bottom = 16.dp)
      ) {
        // Avatar overlapping banner
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Box(
            modifier = Modifier
              .offset(y = (-36).dp)
              .size(76.dp)
          ) {
            // Circular Avatar
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(DiscordCardBg)
                .border(4.dp, DiscordCardBg, CircleShape)
            ) {
              if (!user.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                  model = ImageRequest.Builder(context)
                    .data(user.avatarUrl)
                    .crossfade(true)
                    .build(),
                  contentDescription = "User Avatar",
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                  contentScale = ContentScale.Crop,
                  error = painterResource(id = R.drawable.ic_launcher_foreground),
                  placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )
              } else {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(DiscordBlurple),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = user.username.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Online status dot
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = (-2).dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(DiscordGreen)
                .border(3.dp, DiscordCardBg, CircleShape)
            )
          }

          // Verified Pill
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = DiscordGreen.copy(alpha = 0.15f),
            modifier = Modifier.offset(y = (-14).dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = DiscordGreen,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = "AUTHENTICATED",
                color = DiscordGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // User Names
        Text(
          text = user.displayName,
          color = DiscordTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = user.tag,
            color = DiscordTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
          if (user.id.isNotBlank()) {
            Text(
              text = "ID: ${user.id}",
              color = DiscordTextMuted,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info card inside
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = DiscordItemBg,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "RICH PRESENCE GATEWAY STATUS",
              color = DiscordTextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(DiscordGreen)
              )
              Text(
                text = "Streaming live to @${user.username}'s profile",
                color = DiscordTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Log out button
        OutlinedButton(
          onClick = onLogout,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordRed),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("button_discord_logout")
        ) {
          Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Log Out from Discord")
        }
      }
    }
  }
}
