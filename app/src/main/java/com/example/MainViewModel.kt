package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.RpcPresetEntity
import com.example.data.model.BadgeType
import com.example.data.model.DiscordUser
import com.example.data.model.MediaType
import com.example.data.model.RpcActivity
import com.example.data.remote.DiscordApiService
import com.example.data.repository.RpcRepository
import com.example.gateway.ConnectionStatus
import com.example.gateway.DiscordGatewayClient
import com.example.gateway.GatewayLogEntry
import com.example.service.DiscordRpcService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application, viewModelScope)
  private val repository = RpcRepository(database.rpcDao())
  private val prefs = application.getSharedPreferences("discord_rpc_prefs", Context.MODE_PRIVATE)
  private val apiService = DiscordApiService()

  val allPresets: StateFlow<List<RpcPresetEntity>> = repository.allPresets
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Default initial activity: The Mentalist (Screenshot 1)
  private val _activeActivity = MutableStateFlow(
    RpcActivity(
      id = 1,
      mediaName = "The Mentalist",
      title = "Rose-Colored Glasses",
      subtitle = "Rigsby must go undercover as an alumnus to inve...",
      mediaType = MediaType.TV_SHOW,
      posterResId = R.drawable.poster_detective,
      posterUri = null,
      isPlaying = false,
      badgeType = BadgeType.PAUSE,
      currentTimeSeconds = 581L, // 9:41
      totalDurationSeconds = 2580L,
      remainingTimeText = "9:41",
      seasonEpisodeText = "S2E11",
      button1Text = "Watch Episode",
      button1Url = "https://www.netflix.com",
      button2Text = "View Series",
      button2Url = "https://www.imdb.com/title/tt1196946/"
    )
  )
  val activeActivity: StateFlow<RpcActivity> = _activeActivity.asStateFlow()

  val gatewayClient = DiscordGatewayClient(viewModelScope, application)
  val connectionStatus: StateFlow<ConnectionStatus> = gatewayClient.status
  val gatewayLogs: StateFlow<List<GatewayLogEntry>> = gatewayClient.logs

  private val _discordToken = MutableStateFlow(prefs.getString("discord_user_token", "") ?: "")
  val discordToken: StateFlow<String> = _discordToken.asStateFlow()

  // User Profile State
  private val _currentUser = MutableStateFlow(
    if (prefs.getBoolean("user_logged_in", false)) {
      DiscordUser(
        id = prefs.getString("user_id", "") ?: "",
        username = prefs.getString("user_username", "DiscordUser") ?: "DiscordUser",
        globalName = prefs.getString("user_global_name", null),
        avatarUrl = prefs.getString("user_avatar_url", null),
        token = prefs.getString("discord_user_token", "") ?: "",
        isLoggedIn = true
      )
    } else {
      DiscordUser()
    }
  )
  val currentUser: StateFlow<DiscordUser> = _currentUser.asStateFlow()

  private val _isVerifyingLogin = MutableStateFlow(false)
  val isVerifyingLogin: StateFlow<Boolean> = _isVerifyingLogin.asStateFlow()

  private val _loginError = MutableStateFlow<String?>(null)
  val loginError: StateFlow<String?> = _loginError.asStateFlow()

  private val _isBroadcasting = MutableStateFlow(false)
  val isBroadcasting: StateFlow<Boolean> = _isBroadcasting.asStateFlow()

  private val _isTicking = MutableStateFlow(true)
  val isTicking: StateFlow<Boolean> = _isTicking.asStateFlow()

  // 24/7 Mode settings
  private val _is247Enabled = MutableStateFlow(prefs.getBoolean("is_24_7_enabled", true))
  val is247Enabled: StateFlow<Boolean> = _is247Enabled.asStateFlow()

  private val _autoStartOnBoot = MutableStateFlow(prefs.getBoolean("auto_start_on_boot", true))
  val autoStartOnBoot: StateFlow<Boolean> = _autoStartOnBoot.asStateFlow()

  private val _loopPlayback = MutableStateFlow(prefs.getBoolean("loop_playback", true))
  val loopPlayback: StateFlow<Boolean> = _loopPlayback.asStateFlow()

  // PreMiD Premium Status Rotator (Auto-Cycle Presence)
  private val _isRotatorActive = MutableStateFlow(prefs.getBoolean("is_rotator_active", false))
  val isRotatorActive: StateFlow<Boolean> = _isRotatorActive.asStateFlow()

  private val _rotatorIntervalMinutes = MutableStateFlow(prefs.getInt("rotator_interval_minutes", 2))
  val rotatorIntervalMinutes: StateFlow<Int> = _rotatorIntervalMinutes.asStateFlow()

  private val _rotatorSecondsRemaining = MutableStateFlow(120)
  val rotatorSecondsRemaining: StateFlow<Int> = _rotatorSecondsRemaining.asStateFlow()

  private val _rotatorQueue = MutableStateFlow<List<RpcPresetEntity>>(emptyList())
  val rotatorQueue: StateFlow<List<RpcPresetEntity>> = _rotatorQueue.asStateFlow()

  private var rotatorIndex = 0

  // 24/7 Uptime Counter
  private val _uptimeSeconds = MutableStateFlow(0L)
  val uptimeSeconds: StateFlow<Long> = _uptimeSeconds.asStateFlow()

  private var tickingJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureDefaultPresets()
    }
    startTicker()

    // If token exists and was logged in, verify profile in background
    if (_currentUser.value.isLoggedIn && _discordToken.value.isNotBlank()) {
      refreshUserProfile()
    }
  }

  private fun startTicker() {
    tickingJob?.cancel()
    tickingJob = viewModelScope.launch(Dispatchers.Default) {
      while (isActive) {
        delay(1000)
        // Track uptime if broadcasting
        if (_isBroadcasting.value) {
          _uptimeSeconds.value += 1
        }

        // PreMiD Status Rotator countdown & cycle
        if (_isRotatorActive.value && _isBroadcasting.value) {
          if (_rotatorSecondsRemaining.value > 1) {
            _rotatorSecondsRemaining.value -= 1
          } else {
            // Trigger rotation
            cycleToNextRotatorPreset()
            _rotatorSecondsRemaining.value = _rotatorIntervalMinutes.value * 60
          }
        }

        if (_isTicking.value && _activeActivity.value.isPlaying) {
          val current = _activeActivity.value
          var newTime = current.currentTimeSeconds + 1
          // 24/7 Seamless playback looping
          if (newTime >= current.totalDurationSeconds && current.totalDurationSeconds > 0) {
            newTime = if (_loopPlayback.value) 0L else current.totalDurationSeconds
          }
          val remainingSec = (current.totalDurationSeconds - newTime).coerceAtLeast(0)
          val remainingStr = RpcActivity.formatSeconds(remainingSec)
          val updated = current.copy(
            currentTimeSeconds = newTime,
            remainingTimeText = remainingStr
          )
          _activeActivity.value = updated
          if (_isBroadcasting.value) {
            gatewayClient.updateActivity(updated)
          }
        }
      }
    }
  }

  fun toggleRotator() {
    val newState = !_isRotatorActive.value
    _isRotatorActive.value = newState
    prefs.edit().putBoolean("is_rotator_active", newState).apply()
    _rotatorSecondsRemaining.value = _rotatorIntervalMinutes.value * 60
    gatewayClient.log("INFO", "PreMiD Status Rotator: ${if (newState) "ENABLED (Every ${_rotatorIntervalMinutes.value}m)" else "DISABLED"}")
  }

  fun setRotatorInterval(minutes: Int) {
    val safeMinutes = minutes.coerceIn(1, 60)
    _rotatorIntervalMinutes.value = safeMinutes
    prefs.edit().putInt("rotator_interval_minutes", safeMinutes).apply()
    _rotatorSecondsRemaining.value = safeMinutes * 60
    gatewayClient.log("INFO", "PreMiD Status Rotator interval set to $safeMinutes min")
  }

  fun addToRotatorQueue(preset: RpcPresetEntity) {
    if (_rotatorQueue.value.none { it.id == preset.id }) {
      _rotatorQueue.value = _rotatorQueue.value + preset
    }
  }

  fun removeFromRotatorQueue(presetId: Long) {
    _rotatorQueue.value = _rotatorQueue.value.filter { it.id != presetId }
  }

  fun clearRotatorQueue() {
    _rotatorQueue.value = emptyList()
  }

  fun skipToNextRotatorPreset() {
    cycleToNextRotatorPreset()
    _rotatorSecondsRemaining.value = _rotatorIntervalMinutes.value * 60
  }

  private fun cycleToNextRotatorPreset() {
    val candidates = if (_rotatorQueue.value.isNotEmpty()) {
      _rotatorQueue.value
    } else {
      allPresets.value
    }
    if (candidates.isEmpty()) return

    rotatorIndex = (rotatorIndex + 1) % candidates.size
    val nextPreset = candidates[rotatorIndex]
    val activity = nextPreset.toRpcActivity()
    _activeActivity.value = activity
    saveActiveStateToPrefs(activity)

    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(activity)
      updateServiceNotification()
    }
    gatewayClient.log("INFO", "PreMiD Rotator: Switched status to '${nextPreset.mediaName} - ${nextPreset.title}'")
  }

  fun applyPreMiDStoreItem(item: com.example.data.model.PreMiDStoreItem) {
    val activity = item.defaultActivity
    _activeActivity.value = activity
    saveActiveStateToPrefs(activity)

    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(activity)
      updateServiceNotification()
    } else if (_is247Enabled.value && _discordToken.value.isNotBlank()) {
      startBroadcast()
    }
    gatewayClient.log("INFO", "PreMiD Store: Activated '${item.name}' (${item.serviceName})")
  }

  fun exportPresetsToJson(): String {
    val presets = allPresets.value
    val jsonArray = org.json.JSONArray()
    presets.forEach { p ->
      val obj = org.json.JSONObject().apply {
        put("presetName", p.presetName)
        put("mediaName", p.mediaName)
        put("title", p.title)
        put("subtitle", p.subtitle)
        put("mediaType", p.mediaType)
        put("activityType", p.activityType)
        put("serviceName", p.serviceName)
        put("button1Text", p.button1Text)
        put("button1Url", p.button1Url)
        put("button2Text", p.button2Text)
        put("button2Url", p.button2Url)
        put("cardTheme", p.cardTheme)
      }
      jsonArray.put(obj)
    }
    return jsonArray.toString(2)
  }

  fun importPresetFromJson(jsonStr: String): Boolean {
    return try {
      val trimmed = jsonStr.trim()
      if (trimmed.startsWith("[")) {
        val array = org.json.JSONArray(trimmed)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          parseAndInsertJsonPreset(obj)
        }
      } else {
        val obj = org.json.JSONObject(trimmed)
        parseAndInsertJsonPreset(obj)
      }
      gatewayClient.log("INFO", "Imported PreMiD presets from JSON")
      true
    } catch (e: Exception) {
      gatewayClient.log("ERROR", "Failed to import JSON: ${e.message}")
      false
    }
  }

  private fun parseAndInsertJsonPreset(obj: org.json.JSONObject) {
    viewModelScope.launch {
      val preset = RpcPresetEntity(
        presetName = obj.optString("presetName", "Imported Preset"),
        mediaName = obj.optString("mediaName", "Custom Presence"),
        title = obj.optString("title", "Presence Title"),
        subtitle = obj.optString("subtitle", "Streaming via PreMiD"),
        mediaType = obj.optString("mediaType", "TV_SHOW"),
        activityType = obj.optString("activityType", "WATCHING"),
        userStatus = "ONLINE",
        posterUri = null,
        posterResName = "poster_detective",
        isPlaying = true,
        badgeType = "PLAY",
        currentTimeSeconds = 300L,
        totalDurationSeconds = 1800L,
        remainingTimeText = "25:00",
        seasonEpisodeText = "PreMiD",
        button1Text = obj.optString("button1Text", "PreMiD Store"),
        button1Url = obj.optString("button1Url", "https://premid.app"),
        button2Text = obj.optString("button2Text", ""),
        button2Url = obj.optString("button2Url", ""),
        serviceName = obj.optString("serviceName", "PreMiD"),
        cardTheme = obj.optString("cardTheme", "DISCORD_DARK")
      )
      repository.insertPreset(preset)
    }
  }

  fun loginWithToken(token: String, onComplete: ((Boolean) -> Unit)? = null) {
    val cleanToken = token.trim()
    if (cleanToken.isBlank()) {
      _loginError.value = "Please enter your Discord token"
      onComplete?.invoke(false)
      return
    }

    viewModelScope.launch {
      _isVerifyingLogin.value = true
      _loginError.value = null
      val result = apiService.fetchCurrentUser(cleanToken)
      _isVerifyingLogin.value = false

      result.onSuccess { user ->
        _currentUser.value = user
        _discordToken.value = cleanToken
        prefs.edit()
          .putBoolean("user_logged_in", true)
          .putString("discord_user_token", cleanToken)
          .putString("user_id", user.id)
          .putString("user_username", user.username)
          .putString("user_global_name", user.globalName)
          .putString("user_avatar_url", user.avatarUrl)
          .apply()
        gatewayClient.log("INFO", "Successfully authenticated as @${user.username} (${user.id})")
        startBroadcast()
        onComplete?.invoke(true)
      }.onFailure { err ->
        _loginError.value = err.message ?: "Authentication failed"
        onComplete?.invoke(false)
      }
    }
  }

  fun loginWithDemoAccount(demoName: String = "DiscordWatcher") {
    val demoUser = DiscordUser(
      id = "109876543210987654",
      username = demoName,
      globalName = "Simon Baker Fan",
      avatarUrl = "https://cdn.discordapp.com/embed/avatars/0.png",
      token = "demo_token_authenticated",
      isLoggedIn = true
    )
    _currentUser.value = demoUser
    _discordToken.value = "demo_token_authenticated"
    prefs.edit()
      .putBoolean("user_logged_in", true)
      .putString("discord_user_token", "demo_token_authenticated")
      .putString("user_id", demoUser.id)
      .putString("user_username", demoUser.username)
      .putString("user_global_name", demoUser.globalName)
      .putString("user_avatar_url", demoUser.avatarUrl)
      .apply()
    gatewayClient.log("INFO", "Logged in with Demo Account @$demoName")
  }

  fun refreshUserProfile() {
    val token = _discordToken.value
    if (token.isBlank() || token.startsWith("demo_")) return
    viewModelScope.launch {
      val result = apiService.fetchCurrentUser(token)
      result.onSuccess { user ->
        _currentUser.value = user
        prefs.edit()
          .putString("user_id", user.id)
          .putString("user_username", user.username)
          .putString("user_global_name", user.globalName)
          .putString("user_avatar_url", user.avatarUrl)
          .apply()
      }
    }
  }

  fun logout() {
    _currentUser.value = DiscordUser()
    _discordToken.value = ""
    prefs.edit()
      .putBoolean("user_logged_in", false)
      .remove("discord_user_token")
      .remove("user_id")
      .remove("user_username")
      .remove("user_global_name")
      .remove("user_avatar_url")
      .apply()
    stopBroadcast()
    gatewayClient.log("INFO", "Logged out from Discord")
  }

  fun clearLoginError() {
    _loginError.value = null
  }

  fun updateActivity(updated: RpcActivity) {
    _activeActivity.value = updated
    saveActiveStateToPrefs(updated)
    // Keep RPC running non-stop: instantly push updated presence over open Gateway socket
    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(updated)
      updateServiceNotification()
    } else if (_is247Enabled.value && _discordToken.value.isNotBlank()) {
      startBroadcast()
    }
  }

  fun togglePlayPause() {
    val current = _activeActivity.value
    val newPlaying = !current.isPlaying
    val newBadge = if (newPlaying) BadgeType.PLAY else BadgeType.PAUSE
    val updated = current.copy(
      isPlaying = newPlaying,
      badgeType = newBadge
    )
    _activeActivity.value = updated
    saveActiveStateToPrefs(updated)
    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(updated)
      updateServiceNotification()
    } else if (_is247Enabled.value && _discordToken.value.isNotBlank()) {
      startBroadcast()
    }
  }

  fun seekTo(seconds: Long) {
    val current = _activeActivity.value
    val safeSeconds = seconds.coerceIn(0L, current.totalDurationSeconds.coerceAtLeast(1L))
    val remainingSec = (current.totalDurationSeconds - safeSeconds).coerceAtLeast(0)
    val remainingStr = RpcActivity.formatSeconds(remainingSec)
    val updated = current.copy(
      currentTimeSeconds = safeSeconds,
      remainingTimeText = remainingStr
    )
    _activeActivity.value = updated
    saveActiveStateToPrefs(updated)
    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(updated)
    }
  }

  fun skipSeconds(delta: Long) {
    val current = _activeActivity.value
    seekTo(current.currentTimeSeconds + delta)
  }

  fun resetPlayback() {
    seekTo(0)
  }

  fun setMediaType(type: MediaType) {
    val current = _activeActivity.value
    val updated = current.copy(mediaType = type)
    _activeActivity.value = updated
    saveActiveStateToPrefs(updated)
    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(updated)
    }
  }

  fun setTicking(enabled: Boolean) {
    _isTicking.value = enabled
  }

  fun set247Mode(enabled: Boolean) {
    _is247Enabled.value = enabled
    prefs.edit().putBoolean("is_24_7_enabled", enabled).apply()
    if (_isBroadcasting.value) {
      updateServiceNotification()
    }
  }

  fun setAutoStartOnBoot(enabled: Boolean) {
    _autoStartOnBoot.value = enabled
    prefs.edit().putBoolean("auto_start_on_boot", enabled).apply()
  }

  fun setLoopPlayback(enabled: Boolean) {
    _loopPlayback.value = enabled
    prefs.edit().putBoolean("loop_playback", enabled).apply()
  }

  fun loadPreset(preset: RpcPresetEntity) {
    val activity = preset.toRpcActivity()
    _activeActivity.value = activity
    saveActiveStateToPrefs(activity)
    if (_isBroadcasting.value) {
      gatewayClient.updateActivity(activity)
      updateServiceNotification()
    } else if (_is247Enabled.value && _discordToken.value.isNotBlank()) {
      startBroadcast()
    }
  }

  fun saveCurrentAsPreset(name: String) {
    viewModelScope.launch {
      val entity = RpcPresetEntity.fromRpcActivity(
        presetName = name,
        activity = _activeActivity.value
      )
      repository.insertPreset(entity)
    }
  }

  fun deletePreset(id: Long) {
    viewModelScope.launch {
      repository.deletePresetById(id)
    }
  }

  fun toggleFavorite(preset: RpcPresetEntity) {
    viewModelScope.launch {
      repository.updatePreset(preset.copy(isFavorite = !preset.isFavorite))
    }
  }

  fun saveToken(token: String) {
    _discordToken.value = token
    prefs.edit().putString("discord_user_token", token).apply()
  }

  fun toggleBroadcast() {
    if (_isBroadcasting.value) {
      stopBroadcast()
    } else {
      startBroadcast()
    }
  }

  fun startBroadcast() {
    _isBroadcasting.value = true
    gatewayClient.startPresence(_activeActivity.value, _discordToken.value)
    updateServiceNotification()
  }

  fun stopBroadcast() {
    _isBroadcasting.value = false
    gatewayClient.stopPresence()
    DiscordRpcService.stopService(getApplication())
  }

  private fun saveActiveStateToPrefs(activity: RpcActivity) {
    prefs.edit()
      .putString("active_media_name", activity.mediaName)
      .putString("active_title", activity.title)
      .apply()
  }

  private fun updateServiceNotification() {
    val act = _activeActivity.value
    val status = when (connectionStatus.value) {
      ConnectionStatus.CONNECTED_GATEWAY -> "Live on Discord Gateway"
      ConnectionStatus.LOCAL_SIMULATION -> "Local Simulation"
      ConnectionStatus.CONNECTING -> "Connecting..."
      ConnectionStatus.RECONNECTING -> "Reconnecting..."
      else -> "Active"
    }
    DiscordRpcService.startService(
      getApplication(),
      mediaName = act.mediaName,
      title = act.title,
      isPlaying = act.isPlaying,
      status = status,
      is247 = _is247Enabled.value
    )
  }

  override fun onCleared() {
    super.onCleared()
    tickingJob?.cancel()
    if (!_is247Enabled.value) {
      gatewayClient.stopPresence()
    }
  }
}
