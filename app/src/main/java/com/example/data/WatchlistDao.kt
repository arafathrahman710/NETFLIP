package com.example.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_videos ORDER BY timestamp DESC")
    fun getAllWatchlistVideos(): Flow<List<WatchlistVideo>>

    @Insert
    suspend fun insertVideo(video: WatchlistVideo)

    @Query("DELETE FROM watchlist_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Int)
}
