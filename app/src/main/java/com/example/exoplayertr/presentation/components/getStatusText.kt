package com.example.exoplayertr.presentation.components

import androidx.compose.runtime.Composable
import com.example.exoplayertr.core.ResponseType
import com.example.exoplayertr.presentation.ChatState

@Composable
internal fun getStatusText(state: ChatState): String {
    return when (state) {
        is ChatState.Idle -> "Tap 'Start Chat' to begin"
        is ChatState.Greeting -> "Greeting..."
        is ChatState.Listening -> "Listening..."
        is ChatState.Responding -> when (state.responseType) {
            ResponseType.GREETING -> "Responding to greeting..."
            ResponseType.WEATHER -> "Talking about weather..."
            ResponseType.GENERAL_RESPONSE -> "Responding..."
            ResponseType.GOODBYE -> "Saying goodbye..."
            ResponseType.FALLBACK -> "Didn't catch that..."
            ResponseType.PROMPT -> "Are you still there ?"
        }
        is ChatState.Goodbye -> "Goodbye!"
        is ChatState.Fallback -> "Didn't catch that..."
        is ChatState.Prompt -> "Are you still there?"
    }
}