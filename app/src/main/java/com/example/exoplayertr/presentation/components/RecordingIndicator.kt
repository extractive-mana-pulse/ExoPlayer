package com.example.exoplayertr.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun RecordingIndicator() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                color = Color(0xFFFF4444),
                shape = CircleShape
            )
    )
}