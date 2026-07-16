#!/bin/bash
sed -i '/var showDownloadDialog by remember { mutableStateOf(false) }/a\
    var showNotificationsDialog by remember { mutableStateOf(false) }' app/src/main/java/com/example/MainActivity.kt

sed -i 's/HomeHeader(onProfileClick = { selectedTab = 3 })/HomeHeader(onProfileClick = { selectedTab = 3 }, onNotificationsClick = { showNotificationsDialog = true })/g' app/src/main/java/com/example/MainActivity.kt

