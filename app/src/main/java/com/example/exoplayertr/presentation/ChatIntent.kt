package com.example.exoplayertr.presentation

sealed interface ChatIntent {
    data object StartChat : ChatIntent
    data object VideoCompleted : ChatIntent
    data class SpeechRecognized(val text: String) : ChatIntent
    data object SpeechRecognitionFailed : ChatIntent
    data object EndChat : ChatIntent
}