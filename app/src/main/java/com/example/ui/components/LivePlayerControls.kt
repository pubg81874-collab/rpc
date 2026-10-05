package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BadgeType
import com.example.data.model.MediaType
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
fun LivePlayerControls(
  activity: RpcActivity,
  isTicking: Boolean,
  onTogglePlayPause: () -> Unit,
  onSeek: (Long) -> Unit,
  onSkipBackward: (Long) -> Unit,
  onSkipForward: (Long) -> Unit,
  onReset: () -> Unit,
  onToggleTicking: (Boolean) -> Unit,
  onChangeMediaType: (MediaType) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("live_player_controls"),
    shape = RoundedCornerShape(14.dp),
    color = DiscordItemBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header row with Mode Selector (TV Show vs Movie)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Playback Simulation",
          color = DiscordTextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = activity.mediaType == MediaType.TV_SHOW,
            onClick = { onChangeMediaType(MediaType.TV_SHOW) },
            label = { Text("TV Show", fontSize = 12.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordBlurple,
              selectedLabelColor = Color.White,
              containerColor = DiscordCardBg,
              labelColor = DiscordTextSecondary
            ),
            modifier = Modifier.testTag("mode_tv_show_chip")
          )
          FilterChip(
            selected = activity.mediaType == MediaType.MOVIE,
            onClick = { onChangeMediaType(MediaType.MOVIE) },
            label = { Text("Movie", fontSize = 12.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordBlurple,
              selectedLabelColor = Color.White,
              containerColor = DiscordCardBg,
              labelColor = DiscordTextSecondary
            ),
            modifier = Modifier.testTag("mode_movie_chip")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Scrub Slider (if duration > 0)
      if (activity.totalDurationSeconds > 0) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Slider(
            value = activity.currentTimeSeconds.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..activity.totalDurationSeconds.toFloat(),
            colors = SliderDefaults.colors(
              thumbColor = DiscordBlurple,
              activeTrackColor = DiscordBlurple,
              inactiveTrackColor = DiscordBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("player_seek_slider")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = activity.formatCurrentTime(),
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = activity.formatTotalDuration(),
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Media Buttons Row: Rewind 15s | Play/Pause | Forward 15s | Reset
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedIconButton(
          onClick = { onSkipBackward(15) },
          colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = DiscordTextPrimary
          ),
          modifier = Modifier
            .size(44.dp)
            .testTag("button_skip_backward")
        ) {
          Icon(
            imageVector = Icons.Default.FastRewind,
            contentDescription = "Rewind 15 seconds"
          )
        }

        FilledIconButton(
          onClick = onTogglePlayPause,
          shape = CircleShape,
          colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (activity.isPlaying) DiscordGreen else DiscordBlurple,
            contentColor = Color.White
          ),
          modifier = Modifier
            .size(54.dp)
            .testTag("button_toggle_play_pause")
        ) {
          Icon(
            imageVector = if (activity.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (activity.isPlaying) "Pause" else "Play",
            modifier = Modifier.size(28.dp)
          )
        }

        OutlinedIconButton(
          onClick = { onSkipForward(15) },
          colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = DiscordTextPrimary
          ),
          modifier = Modifier
            .size(44.dp)
            .testTag("button_skip_forward")
        ) {
          Icon(
            imageVector = Icons.Default.FastForward,
            contentDescription = "Forward 15 seconds"
          )
        }

        OutlinedIconButton(
          onClick = onReset,
          colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = DiscordTextSecondary
          ),
          modifier = Modifier
            .size(44.dp)
            .testTag("button_reset_playback")
        ) {
          Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = "Reset to beginning"
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Auto-tick switch (real-time playback increment)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Auto-advance timer",
            color = DiscordTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = if (isTicking) "Running every 1s" else "Paused",
            color = DiscordTextSecondary,
            fontSize = 11.sp
          )
        }

        Switch(
          checked = isTicking,
          onCheckedChange = onToggleTicking,
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = DiscordGreen,
            uncheckedThumbColor = DiscordTextSecondary,
            uncheckedTrackColor = DiscordCardBg
          ),
          modifier = Modifier.testTag("switch_auto_tick")
        )
      }
    }
  }
}
