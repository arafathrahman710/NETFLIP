#!/bin/bash
find . -type f -name "*.kt" -o -name "*.xml" | xargs grep -i -l "terousd"
