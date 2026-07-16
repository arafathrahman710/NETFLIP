package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedVideoDao {
    @Query("SELECT * FROM saved_videos ORDER BY timestamp DESC")
    fun getAllSavedVideos(): Flow<List<SavedVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: SavedVideo)

    @Query("DELETE FROM saved_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Int)
}
