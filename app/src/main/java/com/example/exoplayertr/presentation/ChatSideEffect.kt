package com.example.exoplayertr.presentation

import androidx.compose.runtime.Stable
import com.example.exoplayertr.core.ResponseType

@Stable
sealed interface ChatSideEffect {
    data class StateChanged(val from: ChatState, val to: ChatState) : ChatSideEffect
    data class SpeechRecognized(val text: String, val responseType: ResponseType) : ChatSideEffect
    data object PermissionRequired : ChatSideEffect
}