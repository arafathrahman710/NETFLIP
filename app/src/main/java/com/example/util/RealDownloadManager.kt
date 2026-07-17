package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.SavedVideo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.UUID

enum class DownloadStatus { DOWNLOADING, COMPLETED, FAILED }

data class ActiveDownload(
    val id: String,
    val title: String,
    val url: String,
    var progress: Float = 0f,
    var speedStr: String = "0 KB/s",
    var status: DownloadStatus = DownloadStatus.DOWNLOADING,
    var fileLocation: String = "",
    val streamUrl: String? = null,
    val systemDownloadId: Long = -1L
)

object RealDownloadManager {
    private val _downloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val downloads: StateFlow<List<ActiveDownload>> = _downloads

    fun startDownload(context: Context, title: String, originalUrl: String, streamUrl: String? = null) {
        if (streamUrl.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                android.widget.Toast.makeText(context, "Cannot download: No direct video link found.", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }

        val id = UUID.randomUUID().toString()
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val fileName = "netflip_${safeTitle}_${System.currentTimeMillis()}.mp4"

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(streamUrl)).apply {
                setTitle(title)
                setDescription("Downloading movie/series")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName)
                
                addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                addRequestHeader("Referer", originalUrl)
                addRequestHeader("Accept", "*/*")
                
                val cookieManager = android.webkit.CookieManager.getInstance()
                val cookie = cookieManager.getCookie(originalUrl) ?: cookieManager.getCookie(streamUrl)
                if (cookie != null) {
                    addRequestHeader("Cookie", cookie)
                }
            }

            val systemId = downloadManager.enqueue(request)

            val newDownload = ActiveDownload(id, title, originalUrl, streamUrl = streamUrl, systemDownloadId = systemId)
            _downloads.value = _downloads.value + newDownload

            trackDownloadProgress(context, id, systemId, title, originalUrl, fileName)
        } catch (e: Exception) {
            Log.e("DownloadManager", "Failed to enqueue download", e)
            val failedDownload = ActiveDownload(id, title, originalUrl, streamUrl = streamUrl, status = DownloadStatus.FAILED, speedStr = "Failed")
            _downloads.value = _downloads.value + failedDownload
            android.widget.Toast.makeText(context, "Download failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun trackDownloadProgress(context: Context, id: String, systemId: Long, title: String, originalUrl: String, fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var isFinished = false
            var lastBytes = 0L
            var lastTime = System.currentTimeMillis()
            var currentSpeedBps = 0f

            while (!isFinished && isActive) {
                val query = DownloadManager.Query().setFilterById(systemId)
                val cursor: Cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    
                    if (statusIndex >= 0 && bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0) {
                        val status = cursor.getInt(statusIndex)
                        val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                        val bytesTotal = cursor.getLong(bytesTotalIndex)

                        val now = System.currentTimeMillis()
                        val timeDiff = (now - lastTime) / 1000f
                        
                        if (timeDiff >= 1.0f) {
                            currentSpeedBps = (bytesDownloaded - lastBytes) / timeDiff
                            lastBytes = bytesDownloaded
                            lastTime = now
                        }

                        val progress = if (bytesTotal > 0) bytesDownloaded.toFloat() / bytesTotal else 0f
                        val speedStr = if (currentSpeedBps > 0) formatSpeed(currentSpeedBps) else "0 KB/s"

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                isFinished = true
                                val uriStringIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                val fileUri = if (uriStringIndex >= 0) cursor.getString(uriStringIndex) else ""
                                
                                updateDownload(id) {
                                    it.copy(progress = 1f, status = DownloadStatus.COMPLETED, fileLocation = fileUri, speedStr = "Completed")
                                }
                                saveToDatabase(context, title, originalUrl, fileUri, null)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                isFinished = true
                                val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                val reason = if (reasonIndex >= 0) cursor.getInt(reasonIndex) else -1
                                Log.e("DownloadManager", "Download failed with reason: $reason")
                                
                                updateDownload(id) {
                                    it.copy(status = DownloadStatus.FAILED, speedStr = "Failed ($reason)")
                                }
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                updateDownload(id) {
                                    it.copy(progress = progress, speedStr = speedStr)
                                }
                            }
                        }
                    }
                }
                cursor.close()
                delay(1000)
            }
        }
    }

    private suspend fun saveToDatabase(context: Context, title: String, url: String, filePath: String, streamUrl: String?) {
        try {
            val db = AppDatabase.getDatabase(context)
            db.savedVideoDao().insertVideo(SavedVideo(title = title, url = url, filePath = filePath, streamUrl = streamUrl))
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Download completed: $title", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (dbEx: Throwable) {
            Log.e("DownloadManager", "Room DB insertion failed", dbEx)
        }
    }

    fun retryDownload(context: Context, id: String) {
        val download = _downloads.value.find { it.id == id } ?: return
        _downloads.value = _downloads.value.filter { it.id != id }
        startDownload(context, download.title, download.url, download.streamUrl)
    }

    fun removeDownload(id: String) {
        val download = _downloads.value.find { it.id == id }
        if (download != null && download.systemDownloadId != -1L) {
            // We could optionally cancel the download in DownloadManager here, but we will just remove it from UI for now.
        }
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
