package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.ui.components.DiscordUserProfileCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)
  val context = LocalContext.current
  val currentUser by viewModel.currentUser.collectAsState()
  val isVerifying by viewModel.isVerifyingLogin.collectAsState()
  val loginError by viewModel.loginError.collectAsState()
  val savedToken by viewModel.discordToken.collectAsState()
  var inputToken by remember(savedToken) { mutableStateOf(savedToken) }
  var isTokenVisible by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            if (currentUser.isLoggedIn) "Discord Account" else "Login with Discord",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DiscordTextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("login_back_button")) {
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
    modifier = modifier.testTag("login_screen")
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (currentUser.isLoggedIn) {
        // Authenticated Profile Section
        item {
          DiscordUserProfileCard(
            user = currentUser,
            onLogout = {
              viewModel.logout()
              Toast.makeText(context, "Logged out from Discord", Toast.LENGTH_SHORT).show()
            },
            onRefresh = {
              viewModel.refreshUserProfile()
              Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            }
          )
        }

        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "PRESENCE SYNC ACTIVE",
                color = DiscordTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Your 'Watching' Rich Presence activity is being sent live to Discord's official Gateway using @${currentUser.username}'s account. Your friends will see it in server member lists and your user profile.",
                color = DiscordTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )
            }
          }
        }
      } else {
        // Logged-out Login Flow
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Discord Icon / Logo representation
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(DiscordBlurple),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountCircle,
                  contentDescription = "Discord",
                  tint = Color.White,
                  modifier = Modifier.size(38.dp)
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Login to Discord",
                color = DiscordTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Connect your account to display Rich Presence cards on your Discord profile 24/7",
                color = DiscordTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        // Login error banner
        if (loginError != null) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = DiscordRed.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, DiscordRed.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = DiscordRed,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = loginError ?: "Login error",
                  color = DiscordRed,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // Token Input Form Card
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Key,
                  contentDescription = null,
                  tint = DiscordBlurple,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "Authorization Token Login",
                  color = DiscordTextPrimary,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Enter your user authorization token. The app connects directly to https://discord.com to fetch your profile.",
                color = DiscordTextSecondary,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.height(12.dp))

              OutlinedTextField(
                value = inputToken,
                onValueChange = {
                  inputToken = it
                  viewModel.clearLoginError()
                },
                placeholder = { Text("Paste your Discord Token here", color = DiscordTextMuted) },
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
                  .testTag("input_login_discord_token")
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = {
                  viewModel.loginWithToken(inputToken) { success ->
                    if (success) {
                      Toast.makeText(context, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                enabled = !isVerifying && inputToken.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = DiscordBlurple,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(46.dp)
                  .testTag("button_login_with_token")
              ) {
                if (isVerifying) {
                  CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Authenticating...")
                } else {
                  Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Login with Discord", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Demo Login button for testing
              OutlinedButton(
                onClick = {
                  viewModel.loginWithDemoAccount("DiscordViewer")
                  Toast.makeText(context, "Logged in as @DiscordViewer (Demo Account)", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordTextPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(44.dp)
                  .testTag("button_login_demo")
              ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Demo Account (No Token Required)", fontSize = 13.sp)
              }
            }
          }
        }

        // Instructions Card
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
                Text("How to get your Discord Token", color = DiscordTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "1. Open Discord in your phone or PC browser (Chrome / Kiwi / Firefox).\n2. Open Developer Tools (F12 or Console).\n3. In Console, run:\n   (webpackChunkdiscord_app.push([[''],{},e=>{m=[];for(let c in e.c)m.push(e.c[c])}]),m).find(m=>m?.exports?.default?.getToken!==void 0).exports.default.getToken()\n4. Copy the returned string and paste it into the field above.",
                color = DiscordTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "🔒 Privacy: Your token stays stored only on this Android device and is transmitted directly to Discord's official Gateway.",
                color = DiscordTextMuted,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }
}
