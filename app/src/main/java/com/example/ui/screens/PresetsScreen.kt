package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.MainViewModel
import com.example.R
import com.example.data.local.RpcPresetEntity
import com.example.data.model.PreMiDStoreCatalog
import com.example.data.model.PreMiDStoreItem
import com.example.data.model.RpcActivity
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
fun PresetsScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  onNavigateToEditor: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)
  val context = LocalContext.current
  val presets by viewModel.allPresets.collectAsState()
  val activeActivity by viewModel.activeActivity.collectAsState()

  // Tab State: 0 = PreMiD Store, 1 = My Library, 2 = Status Rotator
  var selectedTab by remember { mutableIntStateOf(0) }
  var storeCategory by remember { mutableStateOf("All") }

  // Rotator State
  val isRotatorActive by viewModel.isRotatorActive.collectAsState()
  val rotatorIntervalMinutes by viewModel.rotatorIntervalMinutes.collectAsState()
  val rotatorSecondsRemaining by viewModel.rotatorSecondsRemaining.collectAsState()
  val rotatorQueue by viewModel.rotatorQueue.collectAsState()

  // Dialogs
  var showSaveCurrentDialog by remember { mutableStateOf(false) }
  var showJsonDialog by remember { mutableStateOf(false) }
  var jsonInputText by remember { mutableStateOf("") }
  var newPresetName by remember { mutableStateOf("") }
  var presetToDelete by remember { mutableStateOf<RpcPresetEntity?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              "PreMiD Presence Hub",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = DiscordTextPrimary
            )
            Text(
              text = if (isRotatorActive) "Auto-Rotator Active (Next in ${RpcActivity.formatSeconds(rotatorSecondsRemaining.toLong())})" else "Store • Presets • 24/7 Rotator",
              fontSize = 11.sp,
              color = if (isRotatorActive) DiscordGreen else DiscordTextSecondary
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("presets_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = DiscordTextPrimary
            )
          }
        },
        actions = {
          // JSON Export/Import Dialog trigger
          IconButton(
            onClick = {
              jsonInputText = viewModel.exportPresetsToJson()
              showJsonDialog = true
            },
            modifier = Modifier.testTag("presets_json_button")
          ) {
            Icon(
              imageVector = Icons.Default.Code,
              contentDescription = "JSON Backup & Import",
              tint = DiscordTextSecondary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DiscordDarkBg)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          newPresetName = "${activeActivity.mediaName} Preset"
          showSaveCurrentDialog = true
        },
        containerColor = DiscordBlurple,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_save_preset")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Save Current Preset"
        )
      }
    },
    containerColor = DiscordDarkBg,
    modifier = modifier.testTag("presets_screen")
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 3-Tab Header: PreMiD Store | My Library | Status Rotator
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = DiscordDarkBg,
        contentColor = DiscordBlurple,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = DiscordBlurple,
            height = 3.dp
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
          text = { Text("PreMiD Store", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
          selectedContentColor = DiscordBlurple,
          unselectedContentColor = DiscordTextSecondary,
          modifier = Modifier.testTag("tab_premid_store")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) },
          text = { Text("My Library (${presets.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
          selectedContentColor = DiscordBlurple,
          unselectedContentColor = DiscordTextSecondary,
          modifier = Modifier.testTag("tab_my_library")
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(18.dp)) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Rotator", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              if (isRotatorActive) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(DiscordGreen)
                )
              }
            }
          },
          selectedContentColor = DiscordBlurple,
          unselectedContentColor = DiscordTextSecondary,
          modifier = Modifier.testTag("tab_rotator")
        )
      }

      when (selectedTab) {
        0 -> {
          // PreMiD Store Tab
          Column(modifier = Modifier.fillMaxSize()) {
            // Category filter chips
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              PreMiDStoreCatalog.categories.forEach { cat ->
                FilterChip(
                  selected = storeCategory == cat,
                  onClick = { storeCategory = cat },
                  label = { Text(cat, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DiscordBlurple,
                    selectedLabelColor = Color.White,
                    containerColor = DiscordItemBg,
                    labelColor = DiscordTextSecondary
                  ),
                  modifier = Modifier.testTag("cat_chip_$cat")
                )
              }
            }

            val filteredItems = if (storeCategory == "All") {
              PreMiDStoreCatalog.items
            } else {
              PreMiDStoreCatalog.items.filter { it.category == storeCategory }
            }

            LazyColumn(
              contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(filteredItems, key = { it.id }) { item ->
                PreMiDStoreItemCard(
                  item = item,
                  isActive = item.defaultActivity.mediaName == activeActivity.mediaName &&
                    item.defaultActivity.title == activeActivity.title,
                  onActivate = {
                    viewModel.applyPreMiDStoreItem(item)
                    Toast.makeText(context, "Activated '${item.name}' presence!", Toast.LENGTH_SHORT).show()
                  },
                  onSaveToLibrary = {
                    val entity = RpcPresetEntity.fromRpcActivity(
                      presetName = item.name,
                      activity = item.defaultActivity,
                      isFavorite = true
                    )
                    viewModel.saveCurrentAsPreset(item.name)
                    Toast.makeText(context, "Saved '${item.name}' to library!", Toast.LENGTH_SHORT).show()
                  },
                  onAddToRotator = {
                    val entity = RpcPresetEntity.fromRpcActivity(
                      presetName = item.name,
                      activity = item.defaultActivity
                    )
                    viewModel.addToRotatorQueue(entity)
                    Toast.makeText(context, "Added to Status Rotator queue!", Toast.LENGTH_SHORT).show()
                  }
                )
              }
            }
          }
        }
        1 -> {
          // My Library Tab
          if (presets.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = null,
                  tint = DiscordTextMuted,
                  modifier = Modifier.size(56.dp)
                )
                Text(
                  text = "No saved presets yet",
                  color = DiscordTextPrimary,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Explore the PreMiD Store tab or tap + to save your current RPC.",
                  color = DiscordTextSecondary,
                  fontSize = 13.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 80.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(presets, key = { it.id }) { preset ->
                val isSelected = preset.mediaName == activeActivity.mediaName && preset.title == activeActivity.title
                PresetItemCard(
                  preset = preset,
                  isSelected = isSelected,
                  onLoad = {
                    viewModel.loadPreset(preset)
                    Toast.makeText(context, "Loaded: ${preset.mediaName}", Toast.LENGTH_SHORT).show()
                  },
                  onToggleFavorite = { viewModel.toggleFavorite(preset) },
                  onAddToRotator = {
                    viewModel.addToRotatorQueue(preset)
                    Toast.makeText(context, "Added '${preset.presetName}' to rotator queue", Toast.LENGTH_SHORT).show()
                  },
                  onDelete = { presetToDelete = preset },
                  onEdit = {
                    viewModel.loadPreset(preset)
                    onNavigateToEditor()
                  }
                )
              }
            }
          }
        }
        2 -> {
          // Status Rotator (24/7 Auto-Cycle) Tab
          RotatorControlScreen(
            viewModel = viewModel,
            isRotatorActive = isRotatorActive,
            rotatorIntervalMinutes = rotatorIntervalMinutes,
            rotatorSecondsRemaining = rotatorSecondsRemaining,
            rotatorQueue = rotatorQueue,
            allPresets = presets
          )
        }
      }
    }
  }

  // Dialog: Save Current RPC as Preset
  if (showSaveCurrentDialog) {
    AlertDialog(
      onDismissRequest = { showSaveCurrentDialog = false },
      title = { Text("Save Current as Preset", color = DiscordTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Give this presence preset a memorable name:", color = DiscordTextSecondary, fontSize = 13.sp)
          OutlinedTextField(
            value = newPresetName,
            onValueChange = { newPresetName = it },
            singleLine = true,
            label = { Text("Preset Name") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DiscordBlurple,
              unfocusedBorderColor = DiscordBorder,
              focusedTextColor = DiscordTextPrimary,
              unfocusedTextColor = DiscordTextPrimary
            ),
            modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPresetName.isNotBlank()) {
              viewModel.saveCurrentAsPreset(newPresetName.trim())
              Toast.makeText(context, "Preset saved to Library!", Toast.LENGTH_SHORT).show()
              showSaveCurrentDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
        ) {
          Text("Save Preset", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSaveCurrentDialog = false }) {
          Text("Cancel", color = DiscordTextSecondary)
        }
      },
      containerColor = DiscordCardBg
    )
  }

  // Dialog: JSON Backup & Import
  if (showJsonDialog) {
    AlertDialog(
      onDismissRequest = { showJsonDialog = false },
      title = { Text("PreMiD JSON Presets", color = DiscordTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Copy your presets or paste a PreMiD JSON payload below to import:", color = DiscordTextSecondary, fontSize = 12.sp)
          OutlinedTextField(
            value = jsonInputText,
            onValueChange = { jsonInputText = it },
            maxLines = 8,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DiscordBlurple,
              unfocusedBorderColor = DiscordBorder,
              focusedTextColor = DiscordTextPrimary,
              unfocusedTextColor = DiscordTextPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .testTag("json_presets_field")
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TextButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("PreMiD Presets JSON", jsonInputText))
                Toast.makeText(context, "Copied JSON to clipboard!", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(Modifier.width(4.dp))
              Text("Copy JSON", fontSize = 12.sp)
            }
            TextButton(
              onClick = {
                val ok = viewModel.importPresetFromJson(jsonInputText)
                if (ok) {
                  Toast.makeText(context, "Presets imported successfully!", Toast.LENGTH_SHORT).show()
                  showJsonDialog = false
                } else {
                  Toast.makeText(context, "Invalid JSON format. Check syntax.", Toast.LENGTH_SHORT).show()
                }
              }
            ) {
              Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(Modifier.width(4.dp))
              Text("Import JSON", fontSize = 12.sp, color = DiscordGreen)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showJsonDialog = false }) {
          Text("Close", color = DiscordTextSecondary)
        }
      },
      containerColor = DiscordCardBg
    )
  }

  // Dialog: Confirm Delete
  presetToDelete?.let { preset ->
    AlertDialog(
      onDismissRequest = { presetToDelete = null },
      title = { Text("Delete Preset?", color = DiscordTextPrimary, fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to remove '${preset.presetName}'?", color = DiscordTextSecondary) },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deletePreset(preset.id)
            presetToDelete = null
            Toast.makeText(context, "Preset deleted", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DiscordRed)
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { presetToDelete = null }) {
          Text("Cancel", color = DiscordTextSecondary)
        }
      },
      containerColor = DiscordCardBg
    )
  }
}

@Composable
fun PreMiDStoreItemCard(
  item: PreMiDStoreItem,
  isActive: Boolean,
  onActivate: () -> Unit,
  onSaveToLibrary: () -> Unit,
  onAddToRotator: () -> Unit
) {
  val context = LocalContext.current
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isActive) DiscordBlurple.copy(alpha = 0.15f) else DiscordCardBg,
    border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, DiscordBlurple) else null,
    modifier = Modifier.fillMaxWidth().testTag("store_item_${item.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Poster / Artwork
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E1F22))
          ) {
            when {
              item.defaultActivity.posterResId != null -> {
                Image(
                  painter = painterResource(id = item.defaultActivity.posterResId),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
              !item.defaultActivity.posterUri.isNullOrBlank() -> {
                AsyncImage(
                  model = ImageRequest.Builder(context).data(item.defaultActivity.posterUri).crossfade(true).build(),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize(),
                  error = painterResource(id = R.drawable.poster_detective)
                )
              }
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = item.name,
                color = DiscordTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (item.isVerified) {
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified PreMiD Presence",
                  tint = DiscordBlurple,
                  modifier = Modifier.size(14.dp)
                )
              }
              if (item.isPremium) {
                Icon(
                  imageVector = Icons.Default.WorkspacePremium,
                  contentDescription = "PreMiD Pro",
                  tint = Color(0xFFFFD700),
                  modifier = Modifier.size(14.dp)
                )
              }
            }
            Text(
              text = "${item.serviceName} • ${item.usersCount} users",
              color = DiscordTextSecondary,
              fontSize = 11.sp
            )
            Text(
              text = item.description,
              color = DiscordTextMuted,
              fontSize = 11.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }

      Spacer(Modifier.height(10.dp))

      // Action row: 1-Tap Activate | Rotator | Save
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onActivate,
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) DiscordGreen else DiscordBlurple,
            contentColor = Color.White
          ),
          modifier = Modifier.weight(1.3f).height(36.dp)
        ) {
          Icon(
            imageVector = if (isActive) Icons.Default.Check else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(Modifier.width(4.dp))
          Text(text = if (isActive) "Active Now" else "Activate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onAddToRotator,
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordTextPrimary),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(4.dp))
          Text("Rotator", fontSize = 11.sp)
        }

        IconButton(
          onClick = onSaveToLibrary,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(Icons.Default.Bookmark, contentDescription = "Save to Library", tint = DiscordTextSecondary)
        }
      }
    }
  }
}

@Composable
fun PresetItemCard(
  preset: RpcPresetEntity,
  isSelected: Boolean,
  onLoad: () -> Unit,
  onToggleFavorite: () -> Unit,
  onAddToRotator: () -> Unit,
  onDelete: () -> Unit,
  onEdit: () -> Unit
) {
  val context = LocalContext.current
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) DiscordBlurple.copy(alpha = 0.2f) else DiscordCardBg,
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, DiscordGreen) else null,
    modifier = Modifier.fillMaxWidth().testTag("preset_card_${preset.id}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E1F22))
          ) {
            when {
              preset.posterResName == "poster_detective" -> {
                Image(
                  painter = painterResource(id = R.drawable.poster_detective),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
              preset.posterResName == "poster_modha_rathri" -> {
                Image(
                  painter = painterResource(id = R.drawable.poster_modha_rathri),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
              !preset.posterUri.isNullOrBlank() -> {
                AsyncImage(
                  model = ImageRequest.Builder(context).data(preset.posterUri).crossfade(true).build(),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
              else -> {
                Image(
                  painter = painterResource(id = R.drawable.poster_detective),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = preset.presetName,
              color = DiscordTextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${preset.mediaName} • ${preset.title}",
              color = DiscordTextSecondary,
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${preset.serviceName} • ${preset.activityType}",
              color = DiscordBlurple,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = if (preset.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (preset.isFavorite) DiscordRed else DiscordTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onLoad,
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DiscordGreen else DiscordButton,
            contentColor = DiscordTextPrimary
          ),
          modifier = Modifier.weight(1.2f).height(34.dp)
        ) {
          Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(15.dp)
          )
          Spacer(Modifier.width(4.dp))
          Text(text = if (isSelected) "Active" else "Load", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = onAddToRotator,
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordTextPrimary),
          modifier = Modifier.weight(1f).height(34.dp)
        ) {
          Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(Modifier.width(4.dp))
          Text("+ Rotator", fontSize = 11.sp)
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
          Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DiscordTextSecondary, modifier = Modifier.size(18.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DiscordRed, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun RotatorControlScreen(
  viewModel: MainViewModel,
  isRotatorActive: Boolean,
  rotatorIntervalMinutes: Int,
  rotatorSecondsRemaining: Int,
  rotatorQueue: List<RpcPresetEntity>,
  allPresets: List<RpcPresetEntity>
) {
  val context = LocalContext.current
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Rotator Master Control Card
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = DiscordCardBg,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PreMiD Status Rotator", color = DiscordTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                  Text("PRO 24/7", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
              Text(
                "Automatically cycles your Discord presence every few minutes without stopping the broadcast!",
                color = DiscordTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
            Switch(
              checked = isRotatorActive,
              onCheckedChange = { viewModel.toggleRotator() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DiscordGreen,
                uncheckedThumbColor = DiscordTextMuted,
                uncheckedTrackColor = DiscordItemBg
              ),
              modifier = Modifier.testTag("rotator_master_switch")
            )
          }

          if (isRotatorActive) {
            Spacer(Modifier.height(14.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = DiscordItemBg,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("NEXT STATUS SWITCH IN:", color = DiscordTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  Text(
                    text = RpcActivity.formatSeconds(rotatorSecondsRemaining.toLong()),
                    color = DiscordGreen,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                }
                Button(
                  onClick = { viewModel.skipToNextRotatorPreset() },
                  colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.testTag("rotator_skip_button")
                ) {
                  Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Next Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // Interval Selector
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = DiscordCardBg,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("ROTATION INTERVAL", color = DiscordTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(1, 2, 5, 10, 15).forEach { min ->
              FilterChip(
                selected = rotatorIntervalMinutes == min,
                onClick = { viewModel.setRotatorInterval(min) },
                label = { Text("${min}m", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = DiscordBlurple,
                  selectedLabelColor = Color.White,
                  containerColor = DiscordItemBg,
                  labelColor = DiscordTextSecondary
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // Rotation Queue List
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (rotatorQueue.isEmpty()) "ROTATION QUEUE (Using All Library Presets)" else "ROTATION QUEUE (${rotatorQueue.size})",
          color = DiscordTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
        if (rotatorQueue.isNotEmpty()) {
          TextButton(onClick = { viewModel.clearRotatorQueue() }) {
            Text("Clear Queue", color = DiscordRed, fontSize = 11.sp)
          }
        }
      }
    }

    if (rotatorQueue.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = DiscordItemBg,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              "Queue is empty — all ${allPresets.size} presets in your Library will cycle automatically.",
              color = DiscordTextSecondary,
              fontSize = 12.sp
            )
            Text(
              "Tip: Go to 'PreMiD Store' or 'My Library' and tap '+ Rotator' to build a custom queue.",
              color = DiscordTextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    } else {
      items(rotatorQueue, key = { it.id }) { preset ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = DiscordItemBg,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(preset.presetName, color = DiscordTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text("${preset.mediaName} • ${preset.title}", color = DiscordTextSecondary, fontSize = 11.sp)
            }
            IconButton(
              onClick = { viewModel.removeFromRotatorQueue(preset.id) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DiscordRed, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}
