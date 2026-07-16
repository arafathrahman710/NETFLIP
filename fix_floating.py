import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_regex = """val isMovieOrSeries = currentUrl.matches(Regex(".*(?:/movie/|/series/|/tv/|/episode/|/watch/|/title/).+"))"""
new_regex = """val isMovieOrSeries = currentUrl.let { url -> 
                                val isMovie = url.contains("/movie/") && !url.endsWith("/movies")
                                val isTvEpisode = url.contains("/tv/") && url.substringAfter("/tv/").contains("-") && url.substringAfter("/tv/").contains("/")
                                val isWatch = url.contains("/watch") || url.contains("/play") || url.contains("/title/")
                                isMovie || isTvEpisode || isWatch
                            }"""

content = content.replace(old_regex, new_regex)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
