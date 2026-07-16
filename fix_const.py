import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("const logoStyle = document.createElement('style');\n                                logoStyle.innerHTML = 'img[alt*=\"terousd\" i]", "const webHeaderStyle = document.createElement('style');\n                                webHeaderStyle.innerHTML = 'img[alt*=\"terousd\" i]")
content = content.replace("document.head.appendChild(logoStyle);\n\n                                const replaceText", "document.head.appendChild(webHeaderStyle);\n\n                                const replaceText")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
