import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_js = """
                                const replaceText = () => {
                                    if (document.title.match(/Terousd|terousd/i)) { document.title = "NETFLIP"; }
                                    const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                                    let node;
                                    while(node = walk.nextNode()) {
                                        if(node.nodeValue.match(/Terousd/gi)) {
                                            node.nodeValue = node.nodeValue.replace(/Terousd/gi, "NETFLIP");
                                        }
                                    }
                                    document.querySelectorAll('*').forEach(el => {
                                        if(el.placeholder && el.placeholder.match(/Terousd/i)) {
                                            el.placeholder = el.placeholder.replace(/Terousd/gi, "NETFLIP");
                                        }
                                        if(el.title && el.title.match(/Terousd/i)) {
                                            el.title = el.title.replace(/Terousd/gi, "NETFLIP");
                                        }
                                        if(el.getAttribute('aria-label') && el.getAttribute('aria-label').match(/Terousd/i)) {
                                            el.setAttribute('aria-label', el.getAttribute('aria-label').replace(/Terousd/gi, "NETFLIP"));
                                        }
                                    });
                                };
"""

import re
content = re.sub(
    r"const replaceText = \(\) => \{[\s\S]*?    \}[\s\S]*?\};",
    new_js.strip(),
    content
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
