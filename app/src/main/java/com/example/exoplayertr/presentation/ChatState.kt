package com.example.exoplayertr.presentation

import androidx.compose.runtime.Stable
import com.example.exoplayertr.core.ResponseType

@Stable
sealed interface ChatState {
    data object Idle : ChatState
    data object Greeting : ChatState
    data object Listening : ChatState
    data class Responding(val responseType: ResponseType) : ChatState
    data object Goodbye : ChatState
    data object Fallback : ChatState
}