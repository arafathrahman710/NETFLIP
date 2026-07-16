package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(enabled: Boolean = true): Modifier = composed {
    if (enabled) {
        this.then(
            Modifier
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
        )
    } else {
        this
    }
}
