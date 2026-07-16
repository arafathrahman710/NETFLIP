#!/bin/bash
sed -i 's/const replaceText = () => {/const replaceText = () => { \n                                      document.title = "NETFLIP";/g' app/src/main/java/com/example/MainActivity.kt
