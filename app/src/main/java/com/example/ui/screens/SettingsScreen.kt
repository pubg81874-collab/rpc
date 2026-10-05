package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.gateway.ConnectionStatus
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBorder
import com.example.ui.theme.DiscordButton
import com.example.ui.theme.DiscordCardBg
import com.example.ui.theme.DiscordDarkBg
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordItemBg
import com.example.ui.theme.DiscordRed
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.DiscordTextSecondary
import com.example.ui.theme.DiscordYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)
  val context = LocalContext.current
  val savedToken by viewModel.discordToken.collectAsState()
  val logs by viewModel.gatewayLogs.collectAsState()
  val connectionStatus by viewModel.connectionStatus.collectAsState()
  val is247Enabled by viewModel.is247Enabled.collectAsState()
  val autoStartOnBoot by viewModel.autoStartOnBoot.collectAsState()
  val loopPlayback by viewModel.loopPlayback.collectAsState()
  val isRotatorActive by viewModel.isRotatorActive.collectAsState()
  val uptimeSeconds by viewModel.uptimeSeconds.collectAsState()

  var inputToken by remember(savedToken) { mutableStateOf(savedToken) }
  var isTokenVisible by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()

  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      listState.animateScrollToItem(logs.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "Discord Gateway & 24/7 Settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DiscordTextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = DiscordTextPrimary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DiscordDarkBg)
      )
    },
    containerColor = DiscordDarkBg,
    modifier = modifier.testTag("settings_screen")
  ) { paddingValues ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Gateway Status Summary Card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "GATEWAY CONNECTION STATUS",
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            val (statusText, statusColor) = when (connectionStatus) {
              ConnectionStatus.CONNECTED_GATEWAY -> Pair("Connected to Discord Gateway (v10)", DiscordGreen)
              ConnectionStatus.LOCAL_SIMULATION -> Pair("Local Simulation Mode (No Token)", DiscordBlurple)
              ConnectionStatus.CONNECTING -> Pair("Connecting to Gateway...", DiscordYellow)
              ConnectionStatus.AUTH_ERROR -> Pair("Authentication Failed (Invalid Token)", DiscordRed)
              ConnectionStatus.DISCONNECTED -> Pair("Disconnected / Socket Closed", DiscordTextMuted)
              ConnectionStatus.RECONNECTING -> Pair("24/7 Auto-Reconnecting to Gateway...", DiscordYellow)
              ConnectionStatus.IDLE -> Pair("Idle / Inactive", DiscordTextSecondary)
            }
            Text(
              text = statusText,
              color = statusColor,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (savedToken.isNotBlank()) "Token configured. 24/7 persistent background watchdog active." else "No token set — operating in local card preview mode.",
              color = DiscordTextMuted,
              fontSize = 12.sp
            )
          }
        }
      }

      // PreMiD Pro VIP Engine Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DiscordItemBg),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth().testTag("premid_pro_engine_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Text("⚡", fontSize = 18.sp)
                }
                Column {
                  Text(
                    text = "PreMiD Pro VIP Engine",
                    color = DiscordTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "High-Performance 24/7 Gateway Node",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "ACTIVATED",
                  color = Color(0xFFFFD700),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(Modifier.height(14.dp))

            // Uptime statistics & Rotator state
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = DiscordCardBg,
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("BROADCAST UPTIME", color = DiscordTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  val hours = uptimeSeconds / 3600
                  val mins = (uptimeSeconds % 3600) / 60
                  val secs = uptimeSeconds % 60
                  Text(
                    text = String.format("%02dh %02dm %02ds", hours, mins, secs),
                    color = DiscordGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = DiscordCardBg,
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("STATUS ROTATOR", color = DiscordTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  Text(
                    text = if (isRotatorActive) "Active (24/7)" else "Standby (Off)",
                    color = if (isRotatorActive) DiscordGreen else DiscordTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(Modifier.height(10.dp))
            Text(
              text = "• PreMiD Presences Store: 10+ official services included\n• Status Rotator: Cycles presences every X minutes\n• Animated GIF avatar & poster artwork support\n• Discord Gateway v10 zero-downtime auto-reconnection",
              color = DiscordTextSecondary,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          }
        }
      }

      // 24/7 Live Configuration Card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AllInclusive,
                contentDescription = null,
                tint = DiscordGreen,
                modifier = Modifier.size(22.dp)
              )
              Text(
                text = "24/7 Always-On Keep-Alive",
                color = DiscordTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Ensures your Discord Rich Presence never stops or gets killed by Android memory managers or device reboots.",
              color = DiscordTextSecondary,
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Setting 1: 24/7 Mode Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "24/7 Persistent Presence",
                  color = DiscordTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Acquires partial wake lock & high-priority foreground service",
                  color = DiscordTextSecondary,
                  fontSize = 11.sp
                )
              }
              Switch(
                checked = is247Enabled,
                onCheckedChange = { viewModel.set247Mode(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = DiscordGreen,
                  uncheckedThumbColor = DiscordTextSecondary,
                  uncheckedTrackColor = DiscordItemBg
                ),
                modifier = Modifier.testTag("switch_24_7_mode")
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Setting 2: Auto-start on reboot
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Auto-Start on Device Boot",
                  color = DiscordTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Automatically resumes Rich Presence after phone restarts",
                  color = DiscordTextSecondary,
                  fontSize = 11.sp
                )
              }
              Switch(
                checked = autoStartOnBoot,
                onCheckedChange = { viewModel.setAutoStartOnBoot(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = DiscordGreen,
                  uncheckedThumbColor = DiscordTextSecondary,
                  uncheckedTrackColor = DiscordItemBg
                ),
                modifier = Modifier.testTag("switch_auto_start_boot")
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Setting 3: Seamless Looping
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Infinite Playback Looping",
                  color = DiscordTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Seamlessly loops episode/movie when finished so presence never pauses",
                  color = DiscordTextSecondary,
                  fontSize = 11.sp
                )
              }
              Switch(
                checked = loopPlayback,
                onCheckedChange = { viewModel.setLoopPlayback(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = DiscordGreen,
                  uncheckedThumbColor = DiscordTextSecondary,
                  uncheckedTrackColor = DiscordItemBg
                ),
                modifier = Modifier.testTag("switch_loop_playback")
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Setting 4: Battery Optimization Exemption button
            OutlinedButton(
              onClick = {
                runCatching {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                  }
                }.onFailure {
                  Toast.makeText(context, "Battery settings not directly accessible", Toast.LENGTH_SHORT).show()
                }
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordYellow),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("button_battery_optimization")
            ) {
              Icon(imageVector = Icons.Default.BatterySaver, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Disable Android Battery Restrictions", fontSize = 13.sp)
            }
          }
        }
      }

      // Discord Token Input Card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = DiscordBlurple,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "Discord User Authorization Token",
                color = DiscordTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Provide your user token to broadcast this 'Watching' Rich Presence live 24/7 to your Discord profile so your friends can see it.",
              color = DiscordTextSecondary,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = inputToken,
              onValueChange = { inputToken = it },
              placeholder = { Text("Paste Discord User Token", color = DiscordTextMuted) },
              singleLine = true,
              visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
              trailingIcon = {
                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                  Icon(
                    imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle token visibility",
                    tint = DiscordTextSecondary
                  )
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DiscordTextPrimary,
                unfocusedTextColor = DiscordTextPrimary,
                focusedContainerColor = DiscordItemBg,
                unfocusedContainerColor = DiscordItemBg,
                focusedBorderColor = DiscordBlurple,
                unfocusedBorderColor = DiscordBorder,
                cursorColor = DiscordBlurple
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("discord_token_input")
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  viewModel.saveToken(inputToken.trim())
                  Toast.makeText(context, "Discord token saved!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = DiscordBlurple,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("save_token_button")
              ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Token")
              }

              OutlinedButton(
                onClick = {
                  inputToken = ""
                  viewModel.saveToken("")
                  Toast.makeText(context, "Token cleared. Switched to Local Mode.", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordRed),
                modifier = Modifier.testTag("clear_token_button")
              ) {
                Text("Clear")
              }
            }
          }
        }
      }

      // Help & Guide on Token
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DiscordItemBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = DiscordYellow, modifier = Modifier.size(18.dp))
              Text("How 24/7 Rich Presence Works", color = DiscordTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "• Android Foreground Service: Keeps a low-impact notification running with partial wake-lock so Android never suspends Gateway WebSocket heartbeats.\n• Auto-Reconnect Engine: Automatically re-establishes connection whenever Wi-Fi or cellular disconnects or reconnects.\n• Auto-Boot: Automatically fires up and resumes Rich Presence right after phone restarts.",
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              lineHeight = 17.sp
            )
          }
        }
      }

      // Real-time Gateway Terminal Log Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = DiscordTextSecondary, modifier = Modifier.size(18.dp))
            Text(
              text = "GATEWAY EVENT LOGS",
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(
            onClick = { viewModel.gatewayClient.clearLogs() },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Clear logs",
              tint = DiscordTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Terminal Box
      if (logs.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF111214),
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "No WebSocket events yet.\nStart Broadcast to view Gateway traffic.",
                color = DiscordTextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      } else {
        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF111214),
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .border(1.dp, DiscordBorder, RoundedCornerShape(8.dp))
          ) {
            val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              items(logs) { entry ->
                val color = when (entry.type) {
                  "OUT" -> DiscordBlurple
                  "IN" -> DiscordGreen
                  "ERROR" -> DiscordRed
                  else -> DiscordTextSecondary
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = timeFormat.format(Date(entry.timestamp)),
                    color = DiscordTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "[${entry.type}]",
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = entry.message,
                    color = DiscordTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
