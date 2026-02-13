package com.example.exoplayertr.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.exoplayertr.presentation.ChatState

@Composable
internal fun AnimatedStatusTextFading(state: ChatState) {
    val statusText = getStatusText(state)
    val hasDots = statusText.contains("...")

    if (hasDots) {
        AnimatedTextWithFadingDots(statusText)
    } else {
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            ),
        )
    }
}

@Composable
private fun AnimatedTextWithFadingDots(text: String) {
    val parts = text.split("...")
    val beforeDots = parts.getOrNull(0) ?: ""
    val afterDots = parts.getOrNull(1) ?: ""

    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotAlphas = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha$index"
        )
    }

    val animatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(beforeDots)
        }

        dotAlphas.forEach { alpha ->
            withStyle(
                style = SpanStyle(
                    color = Color.White.copy(alpha = alpha.value),
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(".")
            }
        }

        if (afterDots.isNotEmpty()) {
            withStyle(
                style = SpanStyle(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(afterDots)
            }
        }
    }

    Text(
        text = animatedText,
        style = MaterialTheme.typography.titleLarge
    )
}