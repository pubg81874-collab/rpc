package com.example.gateway

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.model.BadgeType
import com.example.data.model.DiscordActivityType
import com.example.data.model.DiscordUserStatus
import com.example.data.model.MediaType
import com.example.data.model.RpcActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.min

enum class ConnectionStatus {
  IDLE,
  LOCAL_SIMULATION,
  CONNECTING,
  CONNECTED_GATEWAY,
  AUTH_ERROR,
  DISCONNECTED,
  RECONNECTING
}

data class GatewayLogEntry(
  val timestamp: Long = System.currentTimeMillis(),
  val type: String, // "INFO", "OUT", "IN", "ERROR"
  val message: String
)

class DiscordGatewayClient(
  private val scope: CoroutineScope,
  context: Context? = null
) {
  companion object {
    private const val TAG = "DiscordRPC"
    private const val GATEWAY_URL = "wss://gateway.discord.gg/?v=10&encoding=json"
  }

  private val okHttpClient = OkHttpClient.Builder()
    .readTimeout(45, TimeUnit.SECONDS)
    .pingInterval(20, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

  private var webSocket: WebSocket? = null
  private var heartbeatJob: Job? = null
  private var reconnectJob: Job? = null
  private var lastSequence: Int? = null
  private var currentToken: String? = null
  private var lastActivity: RpcActivity? = null
  private var sessionId: String? = null
  private var resumeGatewayUrl: String? = null
  private var reconnectAttempts = 0
  private var isAutoReconnectActive = true
  private var lastHeartbeatSentTime = 0L
  private var lastHeartbeatAckTime = 0L

  private val _status = MutableStateFlow(ConnectionStatus.IDLE)
  val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

  private val _logs = MutableStateFlow<List<GatewayLogEntry>>(emptyList())
  val logs: StateFlow<List<GatewayLogEntry>> = _logs.asStateFlow()

  init {
    // Network connectivity listener for 24/7 zero-downtime auto-reconnection
    context?.let { ctx ->
      try {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val request = NetworkRequest.Builder()
          .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          .build()
        connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) {
            log("INFO", "Internet network available - checking connection...")
            if (isAutoReconnectActive && !currentToken.isNullOrBlank() &&
              (_status.value == ConnectionStatus.DISCONNECTED || _status.value == ConnectionStatus.RECONNECTING)
            ) {
              scheduleReconnect(1000)
            }
          }

          override fun onLost(network: Network) {
            log("INFO", "Network connection lost - waiting to restore...")
          }
        })
      } catch (e: Exception) {
        log("INFO", "Network callback setup: ${e.message}")
      }
    }
  }

  fun log(type: String, message: String) {
    Log.d(TAG, "[$type] $message")
    val newEntry = GatewayLogEntry(type = type, message = message)
    val current = _logs.value.takeLast(99)
    _logs.value = current + newEntry
  }

  fun clearLogs() {
    _logs.value = emptyList()
  }

  fun startPresence(activity: RpcActivity, token: String? = null) {
    lastActivity = activity
    currentToken = token?.trim()
    isAutoReconnectActive = true
    reconnectAttempts = 0

    if (currentToken.isNullOrBlank()) {
      disconnectSocket()
      _status.value = ConnectionStatus.LOCAL_SIMULATION
      log("INFO", "Active in 24/7 Local Simulation mode (No Discord token provided)")
      return
    }

    _status.value = ConnectionStatus.CONNECTING
    log("INFO", "Connecting to Discord Gateway (v10) for 24/7 broadcast...")
    connectWebSocket()
  }

  fun stopPresence() {
    isAutoReconnectActive = false
    reconnectJob?.cancel()
    reconnectJob = null
    disconnectSocket()
    _status.value = ConnectionStatus.IDLE
    log("INFO", "24/7 Presence stopped by user")
  }

  fun updateActivity(activity: RpcActivity) {
    lastActivity = activity
    if (_status.value == ConnectionStatus.CONNECTED_GATEWAY && webSocket != null) {
      sendPresenceUpdate(activity)
    }
  }

  private fun connectWebSocket() {
    disconnectSocket()
    val targetUrl = resumeGatewayUrl ?: GATEWAY_URL
    val request = Request.Builder()
      .url(targetUrl)
      .build()

    webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(ws: WebSocket, response: Response) {
        log("IN", "WebSocket connected to Gateway: ${response.code} OK")
        reconnectAttempts = 0
      }

      override fun onMessage(ws: WebSocket, text: String) {
        handleGatewayMessage(text)
      }

      override fun onClosing(ws: WebSocket, code: Int, reason: String) {
        log("INFO", "Gateway closing: code=$code ($reason)")
      }

      override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        log("INFO", "Gateway closed: code=$code")
        handleDisconnect(code)
      }

      override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
        log("ERROR", "Gateway connection failure: ${t.localizedMessage}")
        handleDisconnect(response?.code ?: 0)
      }
    })
  }

  private fun handleDisconnect(code: Int) {
    if (!isAutoReconnectActive) return
    // 4004 is Discord invalid token: do not spam reconnect
    if (code == 4004) {
      _status.value = ConnectionStatus.AUTH_ERROR
      log("ERROR", "Invalid Discord token (Error 4004). Please check token.")
      return
    }
    _status.value = ConnectionStatus.RECONNECTING
    // Exponential backoff for 24/7 reconnection (1s -> 2s -> 4s -> ... max 30s)
    val backoffSeconds = min(30L, (1L shl min(reconnectAttempts, 5)))
    reconnectAttempts++
    log("INFO", "Scheduling 24/7 auto-reconnect in ${backoffSeconds}s (attempt #$reconnectAttempts)...")
    scheduleReconnect(backoffSeconds * 1000)
  }

  private fun scheduleReconnect(delayMs: Long) {
    reconnectJob?.cancel()
    reconnectJob = scope.launch(Dispatchers.IO) {
      delay(delayMs)
      if (isAutoReconnectActive && !currentToken.isNullOrBlank()) {
        log("INFO", "Reconnecting to Gateway now...")
        _status.value = ConnectionStatus.CONNECTING
        connectWebSocket()
      }
    }
  }

  private fun handleGatewayMessage(jsonStr: String) {
    try {
      val json = JSONObject(jsonStr)
      val op = json.optInt("op", -1)
      val s = if (json.has("s") && !json.isNull("s")) json.getInt("s") else null
      if (s != null) lastSequence = s
      val t = json.optString("t", "")

      when (op) {
        10 -> { // HELLO
          val d = json.getJSONObject("d")
          val heartbeatInterval = d.getLong("heartbeat_interval")
          log("IN", "Gateway HELLO! Heartbeat interval: ${heartbeatInterval}ms")
          startHeartbeat(heartbeatInterval)
          if (sessionId != null && lastSequence != null) {
            sendResume()
          } else {
            sendIdentify()
          }
        }
        11 -> { // HEARTBEAT ACK
          lastHeartbeatAckTime = System.currentTimeMillis()
          log("IN", "Heartbeat ACK received (latency: ${lastHeartbeatAckTime - lastHeartbeatSentTime}ms)")
        }
        7 -> { // RECONNECT
          log("INFO", "Discord requested RECONNECT (Opcode 7). Reconnecting...")
          connectWebSocket()
        }
        9 -> { // INVALID SESSION
          val resumable = json.optBoolean("d", false)
          log("INFO", "Invalid Session (Opcode 9). Resumable=$resumable")
          if (!resumable) {
            sessionId = null
            lastSequence = null
          }
          scheduleReconnect(1500)
        }
        0 -> { // DISPATCH
          if (t == "READY") {
            val d = json.optJSONObject("d")
            sessionId = d?.optString("session_id")
            resumeGatewayUrl = d?.optString("resume_gateway_url")
            val user = d?.optJSONObject("user")
            val username = user?.optString("username", "User") ?: "User"
            log("INFO", "Gateway READY! Authenticated as @$username [24/7 Presence Live]")
            _status.value = ConnectionStatus.CONNECTED_GATEWAY
            lastActivity?.let { sendPresenceUpdate(it) }
          } else if (t == "RESUMED") {
            log("INFO", "Gateway Session RESUMED successfully")
            _status.value = ConnectionStatus.CONNECTED_GATEWAY
            lastActivity?.let { sendPresenceUpdate(it) }
          }
        }
      }
    } catch (e: Exception) {
      log("ERROR", "Error parsing gateway message: ${e.message}")
    }
  }

  private fun startHeartbeat(intervalMs: Long) {
    heartbeatJob?.cancel()
    heartbeatJob = scope.launch(Dispatchers.IO) {
      while (isActive) {
        delay(intervalMs)
        sendHeartbeat()
      }
    }
  }

  private fun sendHeartbeat() {
    lastHeartbeatSentTime = System.currentTimeMillis()
    val payload = JSONObject().apply {
      put("op", 1)
      put("d", lastSequence ?: JSONObject.NULL)
    }
    webSocket?.send(payload.toString())
    log("OUT", "Sent Heartbeat (seq=$lastSequence)")
  }

  private fun sendIdentify() {
    val token = currentToken ?: return
    try {
      val payload = JSONObject().apply {
        put("op", 2)
        put("d", JSONObject().apply {
          put("token", token)
          put("capabilities", 16381)
          put("properties", JSONObject().apply {
            put("os", "Android")
            put("browser", "Discord Android")
            put("device", "Android")
            put("system_locale", "en-US")
            put("client_version", "210.0")
          })
          lastActivity?.let { activity ->
            put("presence", buildPresencePayload(activity))
          }
        })
      }
      webSocket?.send(payload.toString())
      log("OUT", "Sent IDENTIFY [***]")
    } catch (e: Exception) {
      log("ERROR", "Failed to build identify packet: ${e.message}")
    }
  }

  private fun sendResume() {
    val token = currentToken ?: return
    try {
      val payload = JSONObject().apply {
        put("op", 6)
        put("d", JSONObject().apply {
          put("token", token)
          put("session_id", sessionId)
          put("seq", lastSequence)
        })
      }
      webSocket?.send(payload.toString())
      log("OUT", "Sent RESUME session=$sessionId seq=$lastSequence")
    } catch (e: Exception) {
      sendIdentify()
    }
  }

  private fun sendPresenceUpdate(activity: RpcActivity) {
    try {
      val payload = JSONObject().apply {
        put("op", 3)
        put("d", buildPresencePayload(activity))
      }
      webSocket?.send(payload.toString())
      log("OUT", "Sent Opcode 3 PRESENCE_UPDATE for '${activity.mediaName}' (${if (activity.isPlaying) "Playing" else "Paused"})")
    } catch (e: Exception) {
      log("ERROR", "Failed to send presence update: ${e.message}")
    }
  }

  private fun buildPresencePayload(activity: RpcActivity): JSONObject {
    val now = System.currentTimeMillis()
    val activityObj = JSONObject().apply {
      put("name", activity.mediaName)
      put("type", activity.activityType.code)
      if (activity.activityType == DiscordActivityType.STREAMING) {
        put("url", if (activity.button1Url.isNotBlank()) activity.button1Url else "https://twitch.tv/discord")
      }
      if (activity.title.isNotBlank()) {
        put("details", activity.title)
      }
      val stateText = when {
        activity.mediaType == MediaType.TV_SHOW && activity.seasonEpisodeText.isNotBlank() ->
          "${activity.seasonEpisodeText} • ${activity.subtitle}"
        else -> activity.subtitle
      }
      if (stateText.isNotBlank()) {
        put("state", stateText)
      }
      if (activity.isPlaying) {
        val startMs = now - (activity.currentTimeSeconds * 1000)
        val endMs = if (activity.totalDurationSeconds > 0) {
          startMs + (activity.totalDurationSeconds * 1000)
        } else null
        val timestamps = JSONObject().apply {
          put("start", startMs)
          if (endMs != null) put("end", endMs)
        }
        put("timestamps", timestamps)
      }
      val assets = JSONObject().apply {
        if (!activity.posterUri.isNullOrBlank()) {
          put("large_image", activity.posterUri)
        } else {
          put("large_image", "mp:external/discord_rpc_poster")
        }
        put("large_text", activity.mediaName)
        val smallImg = when (activity.badgeType) {
          BadgeType.PLAY -> "play"
          BadgeType.PAUSE -> "pause"
          BadgeType.NONE -> null
        }
        if (smallImg != null) {
          put("small_image", smallImg)
          put("small_text", if (activity.isPlaying) "Playing" else "Paused")
        }
      }
      put("assets", assets)
      val buttons = JSONArray()
      if (activity.button1Text.isNotBlank()) {
        buttons.put(activity.button1Text)
      }
      if (activity.button2Text.isNotBlank()) {
        buttons.put(activity.button2Text)
      }
      if (buttons.length() > 0) {
        put("buttons", buttons)
      }
    }
    return JSONObject().apply {
      put("since", 0)
      put("activities", JSONArray().put(activityObj))
      put("status", activity.userStatus.code)
      put("afk", activity.userStatus == DiscordUserStatus.IDLE)
    }
  }

  private fun disconnectSocket() {
    heartbeatJob?.cancel()
    heartbeatJob = null
    webSocket?.close(1000, "Disconnect")
    webSocket = null
  }
}
