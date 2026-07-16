#!/bin/bash
sed -i 's/Icon(Icons.Default.Person, contentDescription = "Settings", tint = Color.White)/Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)/g' app/src/main/java/com/example/MainActivity.kt
