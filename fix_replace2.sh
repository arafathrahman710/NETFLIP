#!/bin/bash
sed -i 's/replace(\/Terousd\/gi, "NETFLIP")/replace(\/Terousd|terousd\/gi, "NETFLIP")/g' app/src/main/java/com/example/MainActivity.kt
