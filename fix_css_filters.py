import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_css = """                                const enhanceStyle = document.createElement("style");
                                enhanceStyle.textContent = `
                                    video {
                                        ${if (isSmartEnhance) "filter: contrast(1.15) saturate(1.2) brightness(1.05) drop-shadow(0 0 5px rgba(255,255,255,0.2)) !important;" else ""}
                                        ${if (isHdr) "filter: contrast(1.25) saturate(1.3) brightness(1.15) !important;" else ""}
                                    }
                                `;"""

new_css = """                                const enhanceStyle = document.createElement("style");
                                let filterString = "";
                                ${if (isSmartEnhance && isHdr) "filterString = 'contrast(1.4) saturate(1.5) brightness(1.2) drop-shadow(0 0 5px rgba(255,255,255,0.2)) !important;';" else if (isSmartEnhance) "filterString = 'contrast(1.15) saturate(1.2) brightness(1.05) drop-shadow(0 0 5px rgba(255,255,255,0.2)) !important;';" else if (isHdr) "filterString = 'contrast(1.25) saturate(1.3) brightness(1.15) !important;';" else ""}
                                if (filterString) {
                                    enhanceStyle.textContent = `video { filter: ${filterString} }`;
                                }
"""

content = content.replace(old_css, new_css)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
