import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace('Text(video.url,', 'Text(video.url.replace("stream.terousd.online", "netflip.com"),')

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
