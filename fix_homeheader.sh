#!/bin/bash
sed -i '/fun HomeHeader(onProfileClick: () -> Unit) {/,/^}/c\
@Composable\
fun HomeHeader(onProfileClick: () -> Unit, onNotificationsClick: () -> Unit) {\
    val activeDownloads by com.example.util.DownloadSimulationManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())\
    Row(\
        modifier = Modifier\
            .fillMaxWidth()\
            .height(56.dp)\
            .background(Color.Transparent)\
            .padding(horizontal = 16.dp),\
        horizontalArrangement = Arrangement.End,\
        verticalAlignment = Alignment.CenterVertically\
    ) {\
        Box {\
            androidx.compose.material3.IconButton(onClick = onNotificationsClick, modifier = Modifier.size(32.dp)) {\
                Icon(\
                    imageVector = Icons.Default.Notifications,\
                    contentDescription = "Notifications",\
                    tint = Color.White,\
                    modifier = Modifier.fillMaxSize()\
                )\
            }\
            if (activeDownloads.isNotEmpty()) {\
                Box(\
                    modifier = Modifier\
                        .align(Alignment.TopEnd)\
                        .size(10.dp)\
                        .background(NetflixRed, androidx.compose.foundation.shape.CircleShape)\
                )\
            }\
        }\
        Spacer(modifier = Modifier.width(16.dp))\
        androidx.compose.material3.IconButton(onClick = onProfileClick, modifier = Modifier.size(32.dp)) {\
            Icon(\
                imageVector = Icons.Default.Person,\
                contentDescription = "Profile",\
                tint = Color.White,\
                modifier = Modifier.fillMaxSize()\
            )\
        }\
    }\
}' app/src/main/java/com/example/MainActivity.kt
