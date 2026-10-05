package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.DiscordActivityType
import com.example.data.model.DiscordUserStatus
import com.example.data.model.PreMiDStoreCatalog
import com.example.data.model.RpcActivity
import com.example.gateway.ConnectionStatus
import com.example.ui.components.DiscordRpcCard
import com.example.ui.components.LivePlayerControls
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBorder
import com.example.ui.theme.DiscordCardBg
import com.example.ui.theme.DiscordDarkBg
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordItemBg
import com.example.ui.theme.DiscordRed
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.DiscordTextSecondary
import com.example.ui.theme.DiscordYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onNavigateToEditor: () -> Unit,
  onNavigateToPresets: () -> Unit,
  onNavigateToSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity by viewModel.activeActivity.collectAsState()
  val connectionStatus by viewModel.connectionStatus.collectAsState()
  val isBroadcasting by viewModel.isBroadcasting.collectAsState()
  val isTicking by viewModel.isTicking.collectAsState()
  val allPresets by viewModel.allPresets.collectAsState()
  val is247Enabled by viewModel.is247Enabled.collectAsState()
  val isRotatorActive by viewModel.isRotatorActive.collectAsState()
  val rotatorSecondsRemaining by viewModel.rotatorSecondsRemaining.collectAsState()

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("home_screen_content"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 24/7 Status banner: Gateway status pill + Broadcast button
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = DiscordItemBg,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Status dot and text
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onNavigateToSettings() }
          ) {
            val (dotColor, statusText) = when (connectionStatus) {
              ConnectionStatus.CONNECTED_GATEWAY -> Pair(DiscordGreen, "Discord Gateway Live")
              ConnectionStatus.LOCAL_SIMULATION -> Pair(DiscordBlurple, "Local Simulation Mode")
              ConnectionStatus.CONNECTING -> Pair(DiscordYellow, "Connecting to Gateway...")
              ConnectionStatus.AUTH_ERROR -> Pair(DiscordRed, "Auth Error (Check Token)")
              ConnectionStatus.DISCONNECTED -> Pair(DiscordYellow, "Reconnecting...")
              ConnectionStatus.RECONNECTING -> Pair(DiscordYellow, "24/7 Auto-Reconnecting...")
              ConnectionStatus.IDLE -> Pair(DiscordTextMuted, "Idle (Not Broadcasting)")
            }
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
            )
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = statusText,
                  color = DiscordTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                if (is247Enabled && isBroadcasting) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(DiscordGreen.copy(alpha = 0.2f))
                      .padding(horizontal = 6.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = "24/7 LIVE",
                      color = DiscordGreen,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
              Text(
                text = if (isBroadcasting) "Broadcast running 24/7 in background" else "Tap Broadcast to start 24/7 presence",
                color = DiscordTextSecondary,
                fontSize = 11.sp
              )
            }
          }

          // Broadcast action button
          Button(
            onClick = { viewModel.toggleBroadcast() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isBroadcasting) DiscordRed else DiscordGreen,
              contentColor = Color.White
            ),
            modifier = Modifier.testTag("broadcast_toggle_button")
          ) {
            Icon(
              imageVector = if (isBroadcasting) Icons.Default.Stop else Icons.Default.Podcasts,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isBroadcasting) "Stop" else "Go Live 24/7",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // PreMiD Status Rotator Banner (If active)
    AnimatedVisibility(visible = isRotatorActive) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = DiscordItemBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DiscordGreen.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToPresets() }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(DiscordGreen)
            )
            Column {
              Text(
                text = "PreMiD Status Rotator Active",
                color = DiscordTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Next switch in: ${RpcActivity.formatSeconds(rotatorSecondsRemaining.toLong())}",
                color = DiscordGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
          IconButton(
            onClick = { viewModel.skipToNextRotatorPreset() },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Next Status",
              tint = DiscordBlurple
            )
          }
        }
      }
    }

    // Card Section Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "DISCORD RICH PRESENCE PREVIEW",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = onNavigateToEditor,
          modifier = Modifier
            .size(32.dp)
            .testTag("edit_rpc_icon_button")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit RPC",
            tint = DiscordTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
        IconButton(
          onClick = {
            val jsonPayload = """
              {
                "name": "${activity.mediaName}",
                "type": 3,
                "details": "${activity.title}",
                "state": "${activity.subtitle}",
                "timestamps": { "start": ${System.currentTimeMillis()} }
              }
            """.trimIndent()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Discord RPC JSON", jsonPayload))
            Toast.makeText(context, "Copied RPC payload to clipboard!", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .size(32.dp)
            .testTag("copy_rpc_icon_button")
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy JSON",
            tint = DiscordTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // The Primary Discord RPC Card
    DiscordRpcCard(
      activity = activity,
      onTogglePlayPause = { viewModel.togglePlayPause() },
      onButtonClick = { buttonLabel ->
        Toast.makeText(context, "Clicked: $buttonLabel", Toast.LENGTH_SHORT).show()
      }
    )

    // Quick Activity Type & Online Status bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(DiscordItemBg)
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DISCORD ACTIVITY & STATUS",
          color = DiscordTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Updates Live • Never Stops",
          color = DiscordGreen,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Activity Type Chips (Watching, Playing, Streaming, Listening, Competing)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        DiscordActivityType.values().forEach { type ->
          FilterChip(
            selected = activity.activityType == type,
            onClick = {
              viewModel.updateActivity(activity.copy(activityType = type))
            },
            label = { Text(type.label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordBlurple,
              selectedLabelColor = Color.White,
              containerColor = DiscordCardBg,
              labelColor = DiscordTextSecondary
            ),
            modifier = Modifier.testTag("activity_type_${type.name}")
          )
        }
      }

      // User Status Chips (Online, Idle, DND, Invisible)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        DiscordUserStatus.values().forEach { status ->
          val statusDot = when (status) {
            DiscordUserStatus.ONLINE -> DiscordGreen
            DiscordUserStatus.IDLE -> DiscordYellow
            DiscordUserStatus.DND -> DiscordRed
            DiscordUserStatus.INVISIBLE -> DiscordTextMuted
          }
          FilterChip(
            selected = activity.userStatus == status,
            onClick = {
              viewModel.updateActivity(activity.copy(userStatus = status))
            },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(statusDot)
              )
            },
            label = { Text(status.label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordCardBg,
              selectedLabelColor = DiscordTextPrimary,
              containerColor = DiscordDarkBg,
              labelColor = DiscordTextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = activity.userStatus == status,
              selectedBorderColor = DiscordBlurple,
              borderColor = DiscordBorder
            ),
            modifier = Modifier.testTag("user_status_${status.name}")
          )
        }
      }
    }

    // Quick Preset Chips (The Mentalist, Modha Rathri, etc.)
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "QUICK SWITCH PRESETS",
          color = DiscordTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
        Text(
          text = "View All (${allPresets.size})",
          color = DiscordBlurple,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable { onNavigateToPresets() }
            .padding(4.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        allPresets.take(6).forEach { preset ->
          val isSelected = preset.mediaName == activity.mediaName && preset.title == activity.title
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) DiscordBlurple.copy(alpha = 0.25f) else DiscordItemBg
            ),
            modifier = Modifier
              .clickable { viewModel.loadPreset(preset) }
              .testTag("preset_chip_${preset.id}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) DiscordGreen else DiscordTextMuted)
              )
              Column {
                Text(
                  text = preset.mediaName,
                  color = DiscordTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = preset.title,
                  color = DiscordTextSecondary,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }

    // PreMiD Store Popular Presences Carousel
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "PREMID STORE POPULAR",
            color = DiscordTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }
        Text(
          text = "Open Store",
          color = DiscordBlurple,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable { onNavigateToPresets() }
            .padding(4.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PreMiDStoreCatalog.items.forEach { storeItem ->
          val isCurrent = storeItem.defaultActivity.mediaName == activity.mediaName &&
            storeItem.defaultActivity.title == activity.title
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isCurrent) DiscordBlurple.copy(alpha = 0.25f) else DiscordItemBg,
            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, DiscordBlurple) else null,
            modifier = Modifier
              .clickable {
                viewModel.applyPreMiDStoreItem(storeItem)
                Toast.makeText(context, "Loaded PreMiD: ${storeItem.name}", Toast.LENGTH_SHORT).show()
              }
              .testTag("store_chip_${storeItem.id}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isCurrent) DiscordGreen else DiscordBlurple)
              )
              Column {
                Text(
                  text = storeItem.name,
                  color = DiscordTextPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = storeItem.serviceName,
                  color = DiscordTextSecondary,
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }
    }

    // Live Player Simulation Controls
    LivePlayerControls(
      activity = activity,
      isTicking = isTicking,
      onTogglePlayPause = { viewModel.togglePlayPause() },
      onSeek = { viewModel.seekTo(it) },
      onSkipBackward = { viewModel.skipSeconds(-it) },
      onSkipForward = { viewModel.skipSeconds(it) },
      onReset = { viewModel.resetPlayback() },
      onToggleTicking = { viewModel.setTicking(it) },
      onChangeMediaType = { viewModel.setMediaType(it) }
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
