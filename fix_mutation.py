import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_js = """
                                // --- Hide the app logo via CSS if possible ---
                                const logoStyle = document.createElement('style');
                                logoStyle.innerHTML = 'img[alt*="Terousd"], img[alt*="terousd"] { display: none !important; }';
                                document.head.appendChild(logoStyle);

                                const replaceText = () => {
                                    if (document.title.match(/Terousd|terousd/i)) { document.title = "NETFLIP"; }
                                    const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                                    let node;
                                    while(node = walk.nextNode()) {
                                        if(node.nodeValue.match(/Terousd|terousd/gi)) {
                                            node.nodeValue = node.nodeValue.replace(/Terousd|terousd/gi, "NETFLIP");
                                        }
                                    }
                                };
                                replaceText();

                                // --- MutationObserver for ads added after page load ---
                                const adSelectors = ['iframe[src*="ad"]', '.adsbygoogle', '[id*="ad-slot"]', '[class*="ad-unit"]'];
                                const observer = new MutationObserver(function(mutations) {
                                  replaceText();
                                  adSelectors.forEach(sel => {
                                    document.querySelectorAll(sel).forEach(el => el.remove());
                                  });
                                  const skipBtn = document.querySelector('.ytp-ad-skip-button, .skip-ad, .video-ad-skip');
                                  if(skipBtn) skipBtn.click();
                                  const vids = document.querySelectorAll('video');
                                  vids.forEach(v => {
                                      if(v.closest('.ad-showing') || document.querySelector('.video-ads') || (v.src && (v.src.includes('1xbet') || v.src.includes('ads') || v.src.includes('aliexpress')))) {
                                          v.muted = true;
                                          v.style.display = 'none';
                                      }
                                  });
                                });
                                observer.observe(document.body, { childList: true, subtree: true, characterData: true });
"""

import re
# Regex to find everything between "// --- MutationObserver" (above setInterval) and the end of the observer setup
# It's better to just use replace on the whole block.
content = re.sub(
    r"setInterval\(function\(\) \{[\s\S]*?observer\.observe\(document\.body, \{ childList: true, subtree: true \}\);",
    new_js.strip(),
    content
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
