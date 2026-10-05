package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RpcDao {
  @Query("SELECT * FROM rpc_presets ORDER BY isFavorite DESC, createdAt DESC")
  fun getAllPresets(): Flow<List<RpcPresetEntity>>

  @Query("SELECT * FROM rpc_presets WHERE id = :id LIMIT 1")
  suspend fun getPresetById(id: Long): RpcPresetEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPreset(preset: RpcPresetEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPresets(presets: List<RpcPresetEntity>)

  @Update
  suspend fun updatePreset(preset: RpcPresetEntity)

  @Delete
  suspend fun deletePreset(preset: RpcPresetEntity)

  @Query("DELETE FROM rpc_presets WHERE id = :id")
  suspend fun deletePresetById(id: Long)

  @Query("SELECT COUNT(*) FROM rpc_presets")
  suspend fun getCount(): Int
}
