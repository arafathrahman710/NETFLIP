package com.example.util

import android.content.Context
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.data.SavedVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveDownload(
    val id: String,
    val title: String,
    val url: String,
    var progress: Float = 0f
)

object DownloadSimulationManager {
    private val _activeDownloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val activeDownloads: StateFlow<List<ActiveDownload>> = _activeDownloads

    fun startDownload(context: Context, title: String, url: String) {
        val id = UUID.randomUUID().toString()
        val download = ActiveDownload(id, title, url, 0f)
        _activeDownloads.value = _activeDownloads.value + download
        Toast.makeText(context, "Download started: $title", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            var progress = 0f
            while (progress < 1f) {
                delay(1000) // Increase progress every second
                progress += kotlin.random.Random.Default.nextFloat() * 0.03f + 0.02f
                if (progress > 1f) progress = 1f
                
                _activeDownloads.value = _activeDownloads.value.map {
                    if (it.id == id) it.copy(progress = progress) else it
                }
            }

            // Finished download
            val db = AppDatabase.getDatabase(context)
            db.savedVideoDao().insertVideo(SavedVideo(title = title, url = url))
            
            // Remove from active
            _activeDownloads.value = _activeDownloads.value.filter { it.id != id }
            
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Download completed: $title", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
