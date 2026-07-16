package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_videos")
data class SavedVideo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val filePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
