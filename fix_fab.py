import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val isMovieOrSeries = currentUrl.contains("movie") || currentUrl.contains("series") || currentUrl.contains("tv") || currentUrl.contains("episode") || currentUrl.contains("watch") || currentUrl.contains("netflip.com/title")',
    'val isMovieOrSeries = currentUrl.matches(Regex(".*(?:/movie/|/series/|/tv/|/episode/|/watch/|/title/).+"))'
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
