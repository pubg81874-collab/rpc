package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BadgeType
import com.example.data.model.MediaType
import com.example.data.model.RpcActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Discord RPC", appName)
  }

  @Test
  fun `verify rpc activity time formatting matches screenshots`() {
    // Timestamps: 01:03:49 and 02:21:55
    val activity = RpcActivity(
      mediaName = "Modha Rathri",
      title = "Modha Rathri",
      subtitle = "2026 • 141 minutes",
      mediaType = MediaType.MOVIE,
      badgeType = BadgeType.PLAY,
      currentTimeSeconds = 3829L, // 1h 3m 49s
      totalDurationSeconds = 8515L // 2h 21m 55s
    )

    assertEquals("01:03:49", activity.formatCurrentTime())
    assertEquals("02:21:55", activity.formatTotalDuration())
    assertEquals(3829f / 8515f, activity.progressFraction, 0.001f)
  }
}
