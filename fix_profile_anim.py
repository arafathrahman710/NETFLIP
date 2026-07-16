import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_anim = """            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "Tab Transition"
            )"""

new_anim = """            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == 3) {
                        (fadeIn(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) + 
                         androidx.compose.animation.slideInVertically(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) { it / 2 }) togetherWith 
                        fadeOut(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut))
                    } else if (initialState == 3) {
                        fadeIn(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) togetherWith 
                        (fadeOut(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) + 
                         androidx.compose.animation.slideOutVertically(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) { it / 2 })
                    } else {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    }
                },
                label = "Tab Transition"
            )"""

content = content.replace(old_anim, new_anim)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
