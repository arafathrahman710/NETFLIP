import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_css = "img[alt*=\"Terousd\"], img[alt*=\"terousd\"], .header-icon-btn, .theme-switcher-btn, .mobile-search-btn, .header-search, .theme-switcher, .theme-toggle, .three-dot-menu-container { display: none !important; }"
content = content.replace("img[alt*=\"Terousd\"], img[alt*=\"terousd\"] { display: none !important; }", new_css)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
