package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordCardBg
import com.example.ui.theme.DiscordDarkBg
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.DiscordTextSecondary
import com.example.ui.theme.MyApplicationTheme

enum class Screen(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  HOME("Live RPC", Icons.Filled.LiveTv, Icons.Outlined.LiveTv, "nav_home"),
  PRESETS("Presets", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_presets"),
  EDITOR("Editor", Icons.Filled.Edit, Icons.Outlined.Edit, "nav_editor"),
  SETTINGS("24/7 Setup", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"),
  ACCOUNT("Account", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "nav_account")
}

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        DiscordRpcApp(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscordRpcApp(viewModel: MainViewModel) {
  var currentScreen by remember { mutableStateOf(Screen.HOME) }
  val context = LocalContext.current
  val currentUser by viewModel.currentUser.collectAsState()

  // Notification Permission for Android 13+
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* Permission result handled */ }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  Scaffold(
    topBar = {
      if (currentScreen == Screen.HOME) {
        TopAppBar(
          title = {
            Text(
              text = "Discord Rich Presence",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = DiscordTextPrimary
            )
          },
          actions = {
            // Login / User Profile pill in TopAppBar
            if (currentUser.isLoggedIn) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = DiscordCardBg,
                modifier = Modifier
                  .padding(end = 12.dp)
                  .clickable { currentScreen = Screen.ACCOUNT }
                  .testTag("topbar_user_profile_pill")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(modifier = Modifier.size(24.dp)) {
                    if (!currentUser.avatarUrl.isNullOrBlank()) {
                      AsyncImage(
                        model = ImageRequest.Builder(context)
                          .data(currentUser.avatarUrl)
                          .crossfade(true)
                          .build(),
                        contentDescription = null,
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.ic_launcher_foreground)
                      )
                    } else {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(DiscordBlurple),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = currentUser.username.take(1).uppercase(),
                          color = Color.White,
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                    // Status dot
                    Box(
                      modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DiscordGreen)
                    )
                  }
                  Text(
                    text = "@${currentUser.username}",
                    color = DiscordTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            } else {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = DiscordBlurple,
                modifier = Modifier
                  .padding(end = 12.dp)
                  .clickable { currentScreen = Screen.ACCOUNT }
                  .testTag("topbar_login_button")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = "Login",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DiscordDarkBg
          ),
          modifier = Modifier.testTag("app_top_bar")
        )
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = Color(0xFF111214),
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("app_bottom_bar")
      ) {
        Screen.values().forEach { screen ->
          val isSelected = currentScreen == screen
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentScreen = screen },
            icon = {
              Icon(
                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = screen.label,
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                text = screen.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = DiscordBlurple,
              selectedTextColor = DiscordBlurple,
              unselectedIconColor = DiscordTextSecondary,
              unselectedTextColor = DiscordTextSecondary,
              indicatorColor = DiscordCardBg
            ),
            modifier = Modifier.testTag(screen.testTag)
          )
        }
      }
    },
    containerColor = DiscordDarkBg,
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    when (currentScreen) {
      Screen.HOME -> {
        HomeScreen(
          viewModel = viewModel,
          onNavigateToEditor = { currentScreen = Screen.EDITOR },
          onNavigateToPresets = { currentScreen = Screen.PRESETS },
          onNavigateToSettings = { currentScreen = Screen.SETTINGS },
          modifier = Modifier.padding(innerPadding)
        )
      }
      Screen.PRESETS -> {
        PresetsScreen(
          viewModel = viewModel,
          onBack = { currentScreen = Screen.HOME },
          onNavigateToEditor = { currentScreen = Screen.EDITOR },
          modifier = Modifier.padding(innerPadding)
        )
      }
      Screen.EDITOR -> {
        EditorScreen(
          viewModel = viewModel,
          onBack = { currentScreen = Screen.HOME },
          modifier = Modifier.padding(innerPadding)
        )
      }
      Screen.SETTINGS -> {
        SettingsScreen(
          viewModel = viewModel,
          onBack = { currentScreen = Screen.HOME },
          modifier = Modifier.padding(innerPadding)
        )
      }
      Screen.ACCOUNT -> {
        LoginScreen(
          viewModel = viewModel,
          onBack = { currentScreen = Screen.HOME },
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
