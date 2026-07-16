import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_js = """
                                const replaceText = () => {
                                    if (document.title.match(/Terousd|terousd/i)) { document.title = "NETFLIP"; }
                                    const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                                    let node;
                                    while(node = walk.nextNode()) {
                                        if(node.nodeValue.match(/Terousd|terousd/gi)) {
                                            node.nodeValue = node.nodeValue.replace(/Terousd|terousd/gi, "NETFLIP");
                                        }
                                    }
                                    document.querySelectorAll('input, textarea').forEach(el => {
                                        if(el.placeholder && el.placeholder.match(/Terousd|terousd/i)) {
                                            el.placeholder = el.placeholder.replace(/Terousd|terousd/gi, "NETFLIP");
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
