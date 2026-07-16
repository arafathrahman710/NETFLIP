#!/bin/bash
cat app/src/main/java/com/example/MainActivity.kt | grep -n "My Profile" -C 5
