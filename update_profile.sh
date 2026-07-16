#!/bin/bash

# Remove the settings logic
sed -i '/var showSettings by remember { mutableStateOf(false) }/,/    }/d' app/src/main/java/com/example/MainActivity.kt

sed -i 's/androidx.compose.material3.IconButton(onClick = { showSettings = true }) {/Box {/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)//g' app/src/main/java/com/example/MainActivity.kt

