package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.MainViewModel
import com.example.R
import com.example.data.model.BadgeType
import com.example.data.model.MediaType
import com.example.data.model.PreMiDCardTheme
import com.example.data.model.RpcActivity
import com.example.ui.components.DiscordRpcCard
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBorder
import com.example.ui.theme.DiscordButton
import com.example.ui.theme.DiscordCardBg
import com.example.ui.theme.DiscordDarkBg
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordItemBg
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.DiscordTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)
  val context = LocalContext.current
  val activeActivity by viewModel.activeActivity.collectAsState()

  // Local editable state initialized with active activity
  var mediaName by remember { mutableStateOf(activeActivity.mediaName) }
  var title by remember { mutableStateOf(activeActivity.title) }
  var subtitle by remember { mutableStateOf(activeActivity.subtitle) }
  var mediaType by remember { mutableStateOf(activeActivity.mediaType) }
  var badgeType by remember { mutableStateOf(activeActivity.badgeType) }
  var isPlaying by remember { mutableStateOf(activeActivity.isPlaying) }
  var posterUri by remember { mutableStateOf(activeActivity.posterUri ?: "") }
  var posterResId by remember { mutableStateOf(activeActivity.posterResId) }
  var isGif by remember { mutableStateOf(activeActivity.isGif) }
  var activityType by remember { mutableStateOf(activeActivity.activityType) }
  var userStatus by remember { mutableStateOf(activeActivity.userStatus) }
  var remainingTimeText by remember { mutableStateOf(activeActivity.remainingTimeText) }
  var seasonEpisodeText by remember { mutableStateOf(activeActivity.seasonEpisodeText) }
  var currentTimeSeconds by remember { mutableStateOf(activeActivity.currentTimeSeconds) }
  var totalDurationSeconds by remember { mutableStateOf(activeActivity.totalDurationSeconds) }
  var button1Text by remember { mutableStateOf(activeActivity.button1Text) }
  var button1Url by remember { mutableStateOf(activeActivity.button1Url) }
  var button2Text by remember { mutableStateOf(activeActivity.button2Text) }
  var button2Url by remember { mutableStateOf(activeActivity.button2Url) }

  // PreMiD Pro fields
  var serviceName by remember { mutableStateOf(activeActivity.serviceName) }
  var cardTheme by remember { mutableStateOf(activeActivity.cardTheme) }
  var partyCurrentText by remember { mutableStateOf(activeActivity.partyCurrent?.toString() ?: "") }
  var partyMaxText by remember { mutableStateOf(activeActivity.partyMax?.toString() ?: "") }
  var isLiveStream by remember { mutableStateOf(activeActivity.isLiveStream) }
  var viewerCountText by remember { mutableStateOf(activeActivity.viewerCount?.toString() ?: "") }

  // Zero-permission modern Android Photo/GIF Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      posterUri = it.toString()
      posterResId = null
      isGif = true // Mark as GIF/Media upload
      Toast.makeText(context, "Loaded animated image / GIF from gallery!", Toast.LENGTH_SHORT).show()
    }
  }

  // Live draft preview
  val draftActivity = RpcActivity(
    id = activeActivity.id,
    mediaName = mediaName,
    title = title,
    subtitle = subtitle,
    mediaType = mediaType,
    activityType = activityType,
    userStatus = userStatus,
    posterUri = posterUri.ifBlank { null },
    posterResId = posterResId,
    isPlaying = isPlaying,
    badgeType = badgeType,
    currentTimeSeconds = currentTimeSeconds,
    totalDurationSeconds = totalDurationSeconds,
    remainingTimeText = remainingTimeText,
    seasonEpisodeText = seasonEpisodeText,
    button1Text = button1Text,
    button1Url = button1Url,
    button2Text = button2Text,
    button2Url = button2Url,
    isGif = isGif || posterUri.contains(".gif", ignoreCase = true),
    serviceName = serviceName,
    partyCurrent = partyCurrentText.toIntOrNull(),
    partyMax = partyMaxText.toIntOrNull(),
    isLiveStream = isLiveStream,
    viewerCount = viewerCountText.toIntOrNull(),
    cardTheme = cardTheme
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "Customize Presence & GIFs",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DiscordTextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = DiscordTextPrimary
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              viewModel.updateActivity(draftActivity)
              Toast.makeText(context, "Applied to Discord Presence!", Toast.LENGTH_SHORT).show()
              onBack()
            },
            modifier = Modifier.testTag("editor_apply_action")
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Apply",
              tint = DiscordGreen
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DiscordDarkBg)
      )
    },
    containerColor = DiscordDarkBg,
    modifier = modifier.testTag("editor_screen")
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Live Preview Header
      Text(
        text = "LIVE CARD PREVIEW",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )

      // Live Card with animated GIF support
      DiscordRpcCard(
        activity = draftActivity,
        onTogglePlayPause = {
          val nextPlaying = !isPlaying
          isPlaying = nextPlaying
          badgeType = if (nextPlaying) BadgeType.PLAY else BadgeType.PAUSE
        }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Section: Upload Image / GIF
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
              imageVector = Icons.Default.Gif,
              contentDescription = null,
              tint = DiscordBlurple,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Upload Image or Animated GIF",
              color = DiscordTextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Select any animated .gif or image from your device photos, or pick from popular streaming loop GIFs.",
            color = DiscordTextSecondary,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = DiscordBlurple,
              contentColor = Color.White
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("button_upload_image_gif")
          ) {
            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload GIF / Image from Phone", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(14.dp))

          // GIF toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Animated GIF Mode",
                color = DiscordTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "Enables animated playback & GIF badge on card",
                color = DiscordTextSecondary,
                fontSize = 11.sp
              )
            }

            Switch(
              checked = isGif || posterUri.contains(".gif", ignoreCase = true),
              onCheckedChange = { isGif = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DiscordGreen,
                uncheckedThumbColor = DiscordTextSecondary,
                uncheckedTrackColor = DiscordItemBg
              ),
              modifier = Modifier.testTag("switch_animated_gif")
            )
          }
        }
      }

      // Quick Animated GIFs & Artwork presets
      Text(
        text = "POPULAR ANIMATED GIFS & POSTERS",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Curated GIF 1: Lofi Anime Chill
        PosterThumbnailChoice(
          title = "Lofi Loop (GIF)",
          url = "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDdiNmNjM2JmNTM1NTliNDQ0YTg1NGFiMzc5MmJhYjI4NjU3YjE1MyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/M9gPBrm4KGjo4/giphy.gif",
          isSelected = posterUri.contains("M9gPBrm4KGjo4"),
          isGif = true,
          onClick = {
            posterResId = null
            posterUri = "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDdiNmNjM2JmNTM1NTliNDQ0YTg1NGFiMzc5MmJhYjI4NjU3YjE1MyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/M9gPBrm4KGjo4/giphy.gif"
            isGif = true
          }
        )

        // Curated GIF 2: Cyberpunk City
        PosterThumbnailChoice(
          title = "Cyberpunk (GIF)",
          url = "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNGIxMzJjNWJmMjUzZjgzMGI3NTg3MzJjODQ5N2NkNTIzODFjNGNjZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/13HgwGsXF0aiGY/giphy.gif",
          isSelected = posterUri.contains("13HgwGsXF0aiGY"),
          isGif = true,
          onClick = {
            posterResId = null
            posterUri = "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNGIxMzJjNWJmMjUzZjgzMGI3NTg3MzJjODQ5N2NkNTIzODFjNGNjZCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/13HgwGsXF0aiGY/giphy.gif"
            isGif = true
          }
        )

        // Curated GIF 3: Retro Synthwave Drive
        PosterThumbnailChoice(
          title = "Synthwave (GIF)",
          url = "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExYzg5MTkyOGU4NGZkZDg1NmFlYzQwNTVhMDdmM2JhMGM5NzM2NjIxYyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/ule4vhcY1xEKQ/giphy.gif",
          isSelected = posterUri.contains("ule4vhcY1xEKQ"),
          isGif = true,
          onClick = {
            posterResId = null
            posterUri = "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExYzg5MTkyOGU4NGZkZDg1NmFlYzQwNTVhMDdmM2JhMGM5NzM2NjIxYyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/ule4vhcY1xEKQ/giphy.gif"
            isGif = true
          }
        )

        // Detective artwork
        PosterThumbnailChoice(
          title = "The Mentalist",
          drawableRes = R.drawable.poster_detective,
          isSelected = posterResId == R.drawable.poster_detective && posterUri.isBlank(),
          onClick = {
            posterResId = R.drawable.poster_detective
            posterUri = ""
            isGif = false
          }
        )

        // Modha Rathri artwork
        PosterThumbnailChoice(
          title = "Modha Rathri",
          drawableRes = R.drawable.poster_modha_rathri,
          isSelected = posterResId == R.drawable.poster_modha_rathri && posterUri.isBlank(),
          onClick = {
            posterResId = R.drawable.poster_modha_rathri
            posterUri = ""
            isGif = false
          }
        )
      }

      EditorTextField(
        label = "Custom Image or Animated GIF URL",
        value = posterUri,
        onValueChange = {
          posterUri = it
          posterResId = null
          if (it.contains(".gif", ignoreCase = true)) {
            isGif = true
          }
        },
        placeholder = "https://example.com/animation.gif",
        testTag = "input_poster_url"
      )

      // Section: Media Type
      Text(
        text = "MEDIA TYPE & LAYOUT",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = mediaType == MediaType.TV_SHOW,
          onClick = { mediaType = MediaType.TV_SHOW },
          label = { Text("TV Show (Badges & Remaining)") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DiscordBlurple,
            selectedLabelColor = Color.White,
            containerColor = DiscordItemBg,
            labelColor = DiscordTextSecondary
          )
        )
        FilterChip(
          selected = mediaType == MediaType.MOVIE,
          onClick = { mediaType = MediaType.MOVIE },
          label = { Text("Movie (Progress Bar)") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DiscordBlurple,
            selectedLabelColor = Color.White,
            containerColor = DiscordItemBg,
            labelColor = DiscordTextSecondary
          )
        )
      }

      // Section: Discord Activity Type
      Text(
        text = "DISCORD ACTIVITY TYPE",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        com.example.data.model.DiscordActivityType.values().forEach { type ->
          FilterChip(
            selected = activityType == type,
            onClick = { activityType = type },
            label = { Text(type.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordBlurple,
              selectedLabelColor = Color.White,
              containerColor = DiscordItemBg,
              labelColor = DiscordTextSecondary
            )
          )
        }
      }

      // Section: Discord Online Status
      Text(
        text = "DISCORD USER STATUS",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        com.example.data.model.DiscordUserStatus.values().forEach { status ->
          val dotColor = when (status) {
            com.example.data.model.DiscordUserStatus.ONLINE -> DiscordGreen
            com.example.data.model.DiscordUserStatus.IDLE -> com.example.ui.theme.DiscordYellow
            com.example.data.model.DiscordUserStatus.DND -> com.example.ui.theme.DiscordRed
            com.example.data.model.DiscordUserStatus.INVISIBLE -> DiscordTextMuted
          }
          FilterChip(
            selected = userStatus == status,
            onClick = { userStatus = status },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(dotColor)
              )
            },
            label = { Text(status.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordBlurple,
              selectedLabelColor = Color.White,
              containerColor = DiscordItemBg,
              labelColor = DiscordTextSecondary
            )
          )
        }
      }

      // Section: Titles
      Text(
        text = "MEDIA DETAILS",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )

      EditorTextField(
        label = "Media / Show Name",
        value = mediaName,
        onValueChange = { mediaName = it },
        placeholder = "e.g. The Mentalist, Modha Rathri",
        testTag = "input_media_name"
      )

      EditorTextField(
        label = "Episode / Movie Title",
        value = title,
        onValueChange = { title = it },
        placeholder = "e.g. Rose-Colored Glasses",
        testTag = "input_title"
      )

      EditorTextField(
        label = "Subtitle / Synopsis / Year & Duration",
        value = subtitle,
        onValueChange = { subtitle = it },
        placeholder = "e.g. 2026 • 141 minutes or episode plot",
        testTag = "input_subtitle"
      )

      // Type-specific inputs
      if (mediaType == MediaType.TV_SHOW) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          EditorTextField(
            label = "Remaining Time (TV)",
            value = remainingTimeText,
            onValueChange = { remainingTimeText = it },
            placeholder = "e.g. 9:41",
            modifier = Modifier.weight(1f),
            testTag = "input_remaining_time"
          )
          EditorTextField(
            label = "Season & Ep Tag",
            value = seasonEpisodeText,
            onValueChange = { seasonEpisodeText = it },
            placeholder = "e.g. S2E11",
            modifier = Modifier.weight(1f),
            testTag = "input_season_ep"
          )
        }
      }

      // Section: Badge Type
      Text(
        text = "POSTER OVERLAY BADGE",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
          BadgeType.PAUSE to "Pause (❚❚)",
          BadgeType.PLAY to "Play (▶)",
          BadgeType.NONE to "None"
        ).forEach { (type, label) ->
          FilterChip(
            selected = badgeType == type,
            onClick = {
              badgeType = type
              isPlaying = (type == BadgeType.PLAY)
            },
            label = { Text(label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DiscordBlurple,
              selectedLabelColor = Color.White,
              containerColor = DiscordItemBg,
              labelColor = DiscordTextSecondary
            )
          )
        }
      }

      // Section: PreMiD Pro Customizer
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordCardBg),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "PREMID PRO CUSTOMIZER",
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("PREMIUM", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Card Theme Picker
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Presence Card Theme", color = DiscordTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              PreMiDCardTheme.values().forEach { theme ->
                FilterChip(
                  selected = cardTheme == theme,
                  onClick = { cardTheme = theme },
                  label = { Text(theme.label, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(theme.glowColorHex),
                    selectedLabelColor = Color.Black,
                    containerColor = DiscordItemBg,
                    labelColor = DiscordTextSecondary
                  ),
                  modifier = Modifier.testTag("theme_chip_${theme.name}")
                )
              }
            }
          }

          // Service Provider Picker
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Streaming Service Provider", color = DiscordTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              listOf("Netflix", "YouTube", "Spotify", "Twitch", "Crunchyroll", "Prime Video", "Disney+", "VS Code", "Steam", "SoundCloud").forEach { s ->
                FilterChip(
                  selected = serviceName == s,
                  onClick = { serviceName = s },
                  label = { Text(s, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DiscordBlurple,
                    selectedLabelColor = Color.White,
                    containerColor = DiscordItemBg,
                    labelColor = DiscordTextSecondary
                  )
                )
              }
            }
            EditorTextField(
              label = "Custom Service Name",
              value = serviceName,
              onValueChange = { serviceName = it },
              placeholder = "e.g. Netflix, YouTube, Spotify",
              testTag = "input_custom_service_name"
            )
          }

          // Live Stream Toggle & Viewer Count
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Live Broadcast Mode", color = DiscordTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
              Text("Displays • LIVE pill and live viewer count", color = DiscordTextSecondary, fontSize = 11.sp)
            }
            Switch(
              checked = isLiveStream,
              onCheckedChange = { isLiveStream = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFED4245),
                uncheckedThumbColor = DiscordTextMuted,
                uncheckedTrackColor = DiscordItemBg
              )
            )
          }

          if (isLiveStream) {
            EditorTextField(
              label = "Viewer Count",
              value = viewerCountText,
              onValueChange = { viewerCountText = it },
              placeholder = "e.g. 24800 (shows 24.8K)",
              testTag = "input_viewer_count"
            )
          }

          // Party / Co-Op Size
          Text("Party / Co-Op Session (Optional)", color = DiscordTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            EditorTextField(
              label = "Party Current",
              value = partyCurrentText,
              onValueChange = { partyCurrentText = it },
              placeholder = "e.g. 3",
              modifier = Modifier.weight(1f),
              testTag = "input_party_current"
            )
            EditorTextField(
              label = "Party Max",
              value = partyMaxText,
              onValueChange = { partyMaxText = it },
              placeholder = "e.g. 5",
              modifier = Modifier.weight(1f),
              testTag = "input_party_max"
            )
          }
        }
      }

      // Section: Action Buttons
      Text(
        text = "DISCORD ACTION BUTTONS",
        color = DiscordTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )

      EditorTextField(
        label = "Button 1 Label",
        value = button1Text,
        onValueChange = { button1Text = it },
        placeholder = "e.g. Watch Episode, Watch Movie",
        testTag = "input_btn1_label"
      )

      EditorTextField(
        label = "Button 1 Target URL",
        value = button1Url,
        onValueChange = { button1Url = it },
        placeholder = "https://netflix.com or streaming link",
        testTag = "input_btn1_url"
      )

      EditorTextField(
        label = "Button 2 Label (Optional)",
        value = button2Text,
        onValueChange = { button2Text = it },
        placeholder = "e.g. View Series, IMDb",
        testTag = "input_btn2_label"
      )

      EditorTextField(
        label = "Button 2 Target URL (Optional)",
        value = button2Url,
        onValueChange = { button2Url = it },
        placeholder = "https://imdb.com/title/...",
        testTag = "input_btn2_url"
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Bottom Actions: Apply button & Save As Preset
      Button(
        onClick = {
          viewModel.updateActivity(draftActivity)
          Toast.makeText(context, "Applied to Discord Presence!", Toast.LENGTH_SHORT).show()
          onBack()
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DiscordBlurple,
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("editor_apply_bottom_button")
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Apply Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }

      OutlinedButton(
        onClick = {
          viewModel.updateActivity(draftActivity)
          viewModel.saveCurrentAsPreset(draftActivity.mediaName)
          Toast.makeText(context, "Saved as preset & applied!", Toast.LENGTH_SHORT).show()
          onBack()
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = DiscordTextPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("editor_save_preset_button")
      ) {
        Icon(imageVector = Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save As New Preset", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun PosterThumbnailChoice(
  title: String,
  drawableRes: Int? = null,
  url: String? = null,
  isSelected: Boolean,
  isGif: Boolean = false,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onClick() }
      .padding(2.dp)
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(RoundedCornerShape(8.dp))
        .border(
          width = if (isSelected) 2.5.dp else 1.dp,
          color = if (isSelected) DiscordBlurple else DiscordBorder,
          shape = RoundedCornerShape(8.dp)
        )
    ) {
      if (drawableRes != null) {
        Image(
          painter = painterResource(id = drawableRes),
          contentDescription = title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else if (!url.isNullOrBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
          contentDescription = title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }

      if (isGif) {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xCC000000))
            .padding(horizontal = 3.dp, vertical = 0.5.dp)
        ) {
          Text(
            text = "GIF",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = title,
      color = if (isSelected) DiscordBlurple else DiscordTextSecondary,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
  }
}

@Composable
fun EditorTextField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  testTag: String = "editor_text_field"
) {
  Column(modifier = modifier) {
    Text(
      text = label,
      color = DiscordTextSecondary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(bottom = 4.dp)
    )
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text(placeholder, color = DiscordTextMuted, fontSize = 13.sp) },
      singleLine = true,
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
        .testTag(testTag)
    )
  }
}
