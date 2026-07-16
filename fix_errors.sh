#!/bin/bash
# 1. Fix Icons.Default.Settings in MainActivity
sed -i 's/Icons.Default.Settings/androidx.compose.material.icons.Icons.Default.Settings/g' app/src/main/java/com/example/MainActivity.kt

# 2. Add @Composable to NetflipWebView
sed -i '/fun NetflipWebView(/i \
@Composable' app/src/main/java/com/example/MainActivity.kt

# 3. Fix random in DownloadSimulationManager
sed -i 's/(0.02f..0.05f).random().toFloat()/kotlin.random.Random.Default.nextFloat() * 0.03f + 0.02f/g' app/src/main/java/com/example/util/DownloadSimulationManager.kt

