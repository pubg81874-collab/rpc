package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [RpcPresetEntity::class],
  version = 4,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun rpcDao(): RpcDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN isGif INTEGER NOT NULL DEFAULT 0")
      }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN activityType TEXT NOT NULL DEFAULT 'WATCHING'")
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN userStatus TEXT NOT NULL DEFAULT 'ONLINE'")
      }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN serviceName TEXT NOT NULL DEFAULT 'Netflix'")
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN partyCurrent INTEGER")
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN partyMax INTEGER")
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN isLiveStream INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN viewerCount INTEGER")
        db.execSQL("ALTER TABLE rpc_presets ADD COLUMN cardTheme TEXT NOT NULL DEFAULT 'DISCORD_DARK'")
      }
    }

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "discord_rpc_database"
        )
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
          .fallbackToDestructiveMigration(true)
          .fallbackToDestructiveMigrationOnDowngrade(true)
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database.rpcDao())
          }
        }
      }

      suspend fun populateInitialData(dao: RpcDao) {
        val defaultPresets = listOf(
          // Preset 1: The Mentalist (Screenshot 1)
          RpcPresetEntity(
            id = 1,
            presetName = "The Mentalist - S2E11",
            mediaName = "The Mentalist",
            title = "Rose-Colored Glasses",
            subtitle = "Rigsby must go undercover as an alumnus to inve...",
            mediaType = "TV_SHOW",
            activityType = "WATCHING",
            userStatus = "ONLINE",
            posterUri = null,
            posterResName = "poster_detective",
            isPlaying = false,
            badgeType = "PAUSE",
            currentTimeSeconds = 581L,
            totalDurationSeconds = 2580L,
            remainingTimeText = "9:41",
            seasonEpisodeText = "S2E11",
            button1Text = "Watch Episode",
            button1Url = "https://www.netflix.com",
            button2Text = "View Series",
            button2Url = "https://www.imdb.com/title/tt1196946/",
            isFavorite = true,
            serviceName = "HBO Max",
            createdAt = System.currentTimeMillis() - 10000
          ),
          // Preset 2: Modha Rathri (Screenshot 2)
          RpcPresetEntity(
            id = 2,
            presetName = "Modha Rathri (2026)",
            mediaName = "Modha Rathri",
            title = "Modha Rathri",
            subtitle = "2026   141 minutes",
            mediaType = "MOVIE",
            activityType = "WATCHING",
            userStatus = "ONLINE",
            posterUri = null,
            posterResName = "poster_modha_rathri",
            isPlaying = true,
            badgeType = "PLAY",
            currentTimeSeconds = 3829L,
            totalDurationSeconds = 8515L,
            remainingTimeText = "01:18:06",
            seasonEpisodeText = "Movie",
            button1Text = "Watch Movie",
            button1Url = "https://www.primevideo.com",
            button2Text = "",
            button2Url = "",
            isFavorite = true,
            serviceName = "Prime Video",
            createdAt = System.currentTimeMillis() - 20000
          )
        )
        dao.insertPresets(defaultPresets)
      }
    }
  }
}
