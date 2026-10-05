package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.service.DiscordRpcService

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
      intent?.action == "android.intent.action.QUICKBOOT_POWERON"
    ) {
      val prefs = context.getSharedPreferences("discord_rpc_prefs", Context.MODE_PRIVATE)
      val is247Enabled = prefs.getBoolean("is_24_7_enabled", true)
      val autoStartOnBoot = prefs.getBoolean("auto_start_on_boot", true)
      val mediaName = prefs.getString("active_media_name", "The Mentalist") ?: "The Mentalist"
      val title = prefs.getString("active_title", "Rose-Colored Glasses") ?: "Rose-Colored Glasses"

      if (is247Enabled && autoStartOnBoot) {
        DiscordRpcService.startService(
          context = context,
          mediaName = mediaName,
          title = title,
          isPlaying = true,
          status = "24/7 Auto-Started after Reboot",
          is247 = true
        )
      }
    }
  }
}
