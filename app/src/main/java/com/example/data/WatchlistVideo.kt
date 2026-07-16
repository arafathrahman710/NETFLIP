package com.example.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "watchlist_videos")
data class WatchlistVideo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)
