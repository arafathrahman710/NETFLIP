import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

import re
content = re.sub(
    r"const logoStyle = document.createElement\('style'\);\s*logoStyle.innerHTML = '(.*?)';\s*document.head.appendChild\(webHeaderStyle\);",
    r"const webHeaderStyle = document.createElement('style');\n                                webHeaderStyle.innerHTML = '\1';\n                                document.head.appendChild(webHeaderStyle);",
    content
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
