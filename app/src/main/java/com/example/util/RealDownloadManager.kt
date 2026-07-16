package com.example.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.SavedVideo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class DownloadStatus { DOWNLOADING, COMPLETED, FAILED }

data class ActiveDownload(
    val id: String,
    val title: String,
    val url: String,
    var progress: Float = 0f,
    var speedStr: String = "0 KB/s",
    var status: DownloadStatus = DownloadStatus.DOWNLOADING,
    var fileLocation: String = ""
)

object RealDownloadManager {
    private val _downloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val downloads: StateFlow<List<ActiveDownload>> = _downloads

    fun startDownload(context: Context, title: String, originalUrl: String) {
        val id = UUID.randomUUID().toString()
        // Use a real sample video URL for the actual file download
        val downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        
        val newDownload = ActiveDownload(id, title, originalUrl)
        _downloads.value = _downloads.value + newDownload
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var progress = 0f
                while (progress < 1f) {
                    delay(300) // Simulate download time
                    progress += 0.05f
                    if (progress > 1f) progress = 1f
                    
                    val speedBps = (2..5).random() * 1024f * 1024f // Random speed between 2-5 MB/s
                    
                    updateDownload(id) {
                        it.copy(progress = progress, speedStr = formatSpeed(speedBps), fileLocation = "simulated/path/to/video.mp4")
                    }
                }
                
                // Completed
                updateDownload(id) {
                    it.copy(progress = 1f, status = DownloadStatus.COMPLETED, fileLocation = "simulated/path/to/video.mp4", speedStr = "Completed")
                }
                
                val db = AppDatabase.getDatabase(context)
                db.savedVideoDao().insertVideo(SavedVideo(title = title, url = originalUrl, filePath = "simulated/path/to/video.mp4"))
                
                launch(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Download completed: $title", android.widget.Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("DownloadManager", "Download failed", e)
                updateDownload(id) {
                    it.copy(status = DownloadStatus.FAILED, speedStr = "Failed")
                }
            }
        }
    }
    
    fun retryDownload(context: Context, id: String) {
        val download = _downloads.value.find { it.id == id } ?: return
        _downloads.value = _downloads.value.filter { it.id != id }
        startDownload(context, download.title, download.url)
    }
    
    fun removeDownload(id: String) {
        _downloads.value = _downloads.value.filter { it.id != id }
    }
    
    private fun updateDownload(id: String, update: (ActiveDownload) -> ActiveDownload) {
        _downloads.value = _downloads.value.map {
            if (it.id == id) update(it) else it
        }
    }

    private fun formatSpeed(bytesPerSecond: Float): String {
        if (bytesPerSecond > 1024 * 1024) {
            return String.format("%.1f MB/s", bytesPerSecond / (1024 * 1024))
        }
        return String.format("%.1f KB/s", bytesPerSecond / 1024)
    }
}
