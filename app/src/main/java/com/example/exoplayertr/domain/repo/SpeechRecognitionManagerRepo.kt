package com.example.exoplayertr.domain.repo

interface SpeechRecognitionManagerRepo {
    fun initialize()
    fun startListening()
    fun stopListening()
    fun cancel()
    fun destroy()
    fun setCallbacks(onResult: (String) -> Unit, onError: () -> Unit) // NEW
}