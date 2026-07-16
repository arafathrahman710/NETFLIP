#!/bin/bash
sed -i 's/if(node.nodeValue.match(\/Terousd\/i)) {/if(node.nodeValue.match(\/Terousd|terousd\/gi)) {/g' app/src/main/java/com/example/MainActivity.kt
