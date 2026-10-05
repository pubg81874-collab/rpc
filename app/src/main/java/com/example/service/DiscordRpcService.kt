package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class DiscordRpcService : Service() {

  companion object {
    const val CHANNEL_ID = "discord_rpc_24_7_channel"
    const val NOTIFICATION_ID = 4042
    const val ACTION_START = "com.example.discordrpc.ACTION_START"
    const val ACTION_STOP = "com.example.discordrpc.ACTION_STOP"

    const val EXTRA_MEDIA_NAME = "extra_media_name"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_IS_PLAYING = "extra_is_playing"
    const val EXTRA_STATUS = "extra_status"
    const val EXTRA_IS_24_7 = "extra_is_24_7"

    fun startService(
      context: Context,
      mediaName: String,
      title: String,
      isPlaying: Boolean,
      status: String,
      is247: Boolean = true
    ) {
      val intent = Intent(context, DiscordRpcService::class.java).apply {
        action = ACTION_START
        putExtra(EXTRA_MEDIA_NAME, mediaName)
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_IS_PLAYING, isPlaying)
        putExtra(EXTRA_STATUS, status)
        putExtra(EXTRA_IS_24_7, is247)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, DiscordRpcService::class.java).apply {
        action = ACTION_STOP
      }
      context.startService(intent)
    }
  }

  private var wakeLock: PowerManager.WakeLock? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    acquireWakeLock()
  }

  private fun acquireWakeLock() {
    try {
      if (wakeLock == null) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
          PowerManager.PARTIAL_WAKE_LOCK,
          "DiscordRPC::24x7PresenceWakeLock"
        )?.apply {
          setReferenceCounted(false)
          acquire()
        }
      }
    } catch (e: Exception) {
      // Best-effort wake lock acquisition
    }
  }

  private fun releaseWakeLock() {
    try {
      wakeLock?.let {
        if (it.isHeld) {
          it.release()
        }
      }
      wakeLock = null
    } catch (e: Exception) {
      // Best-effort release
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_STOP -> {
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        return START_NOT_STICKY
      }
      ACTION_START -> {
        val mediaName = intent.getStringExtra(EXTRA_MEDIA_NAME) ?: "The Mentalist"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Rose-Colored Glasses"
        val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
        val status = intent.getStringExtra(EXTRA_STATUS) ?: "Broadcasting"
        val is247 = intent.getBooleanExtra(EXTRA_IS_24_7, true)

        if (wakeLock?.isHeld != true) {
          acquireWakeLock()
        }

        val notification = buildNotification(mediaName, title, isPlaying, status, is247)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
          )
        } else {
          startForeground(NOTIFICATION_ID, notification)
        }
      }
    }
    // Return START_STICKY so Android system restarts the service if memory pressure terminates it!
    return START_STICKY
  }

  private fun buildNotification(
    mediaName: String,
    title: String,
    isPlaying: Boolean,
    status: String,
    is247: Boolean
  ): Notification {
    val openAppIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      this, 0, openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, DiscordRpcService::class.java).apply {
      action = ACTION_STOP
    }
    val stopPendingIntent = PendingIntent.getService(
      this, 1, stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val modeBadge = if (is247) "[24/7 ACTIVE] " else ""

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("${modeBadge}Watching $mediaName")
      .setContentText("$title • $status")
      .setContentIntent(contentPendingIntent)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        "Stop Presence",
        stopPendingIntent
      )
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Discord RPC 24/7 Keep-Alive",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Keeps Discord Rich Presence active 24/7 in the background"
        setShowBadge(true)
      }
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager?.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    releaseWakeLock()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
