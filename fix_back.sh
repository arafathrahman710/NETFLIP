#!/bin/bash
sed -i '/BackHandler(enabled = true) {/,/    }/c\
    BackHandler(enabled = true) {\
        if (fullScreenHelper?.isFullScreen() == true) {\
            fullScreenHelper?.hideCustomView()\
        } else if (selectedTab != 0) {\
            selectedTab = 0\
        } else {\
            webViewRef?.evaluateJavascript(\
                "(function() { if (window.location.pathname !== \\"/\\" && window.location.pathname !== \\"\\") { window.history.back(); return \\"true\\"; } else { return \\"false\\"; } })();"\
            ) { result ->\
                if (result == "\\"false\\"") {\
                    if (webViewRef?.canGoBack() == true) {\
                        webViewRef?.goBack()\
                    } else {\
                        activity?.finish()\
                    }\
                }\
            }\
        }\
    }' app/src/main/java/com/example/MainActivity.kt
