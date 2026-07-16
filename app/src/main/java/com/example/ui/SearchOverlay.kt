package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.SearchHistory
import com.example.ui.theme.NetflixDark
import com.example.ui.theme.NetflixDarker
import com.example.ui.theme.NetflixGrayText
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.glassmorphism
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SearchOverlay(onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val searchHistory by db.searchHistoryDao().getRecentSearches().collectAsStateWithLifecycle(initialValue = emptyList())
    var showHistory by remember { mutableStateOf(true) }

    val isGlassmorphism = com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false).value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isGlassmorphism) Color.Transparent else NetflixDark)
            .glassmorphism(isGlassmorphism)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(16.dp)
        ) {
            BasicTextField(
                value = query,
                onValueChange = { 
                    query = it 
                    showHistory = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(NetflixDarker, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(NetflixRed),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        if (query.isNotBlank()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.searchHistoryDao().insertSearch(SearchHistory(query = query))
                            }
                            onSearch(query)
                            showHistory = false
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NetflixGrayText,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text("Search movies, shows...", color = NetflixGrayText, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    query = "" 
                                    showHistory = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = NetflixGrayText)
                            }
                        }
                    }
                }
            )
        }
        
        if (showHistory && searchHistory.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isGlassmorphism) Color.Transparent else NetflixDarker)
            ) {
                items(searchHistory) { history ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = history.query
                                onSearch(history.query)
                                showHistory = false
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.searchHistoryDao().insertSearch(SearchHistory(query = history.query)) // Refresh timestamp
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NetflixGrayText)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(history.query, color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.searchHistoryDao().deleteSearch(history.query)
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = NetflixGrayText)
                        }
                    }
                }
            }
        }
    }
}
