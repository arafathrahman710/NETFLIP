#!/bin/bash

sed -i '/const replaceText = () => {/,/}, 500);/c\
                                  const replaceText = () => {\
                                      const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);\
                                      let node;\
                                      while(node = walk.nextNode()) {\
                                          if(node.nodeValue.match(/Terousd/i)) {\
                                              node.nodeValue = node.nodeValue.replace(/Terousd/gi, "NETFLIP");\
                                          }\
                                      }\
                                      if (logoStyle.parentNode) logoStyle.parentNode.removeChild(logoStyle);\
                                  };\
                                  replaceText();\
                                }, 500);' app/src/main/java/com/example/MainActivity.kt
