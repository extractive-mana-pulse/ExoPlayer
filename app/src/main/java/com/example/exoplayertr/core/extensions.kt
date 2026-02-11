package com.example.exoplayertr.core

import android.content.Context
import android.widget.Toast
import com.example.exoplayertr.domain.model.VideoConfig
import com.example.exoplayertr.presentation.ChatState

fun ChatState.toVideoConfig(): VideoConfig = when (this) {
    is ChatState.Idle -> VideoConfig("idle".lowercase(), shouldLoop = true)
    is ChatState.Greeting -> VideoConfig("greeting".lowercase(), shouldLoop = false)
    is ChatState.Listening -> VideoConfig("listening".lowercase(), shouldLoop = true)
    is ChatState.Responding -> VideoConfig(responseType.name.lowercase(), shouldLoop = false)
    is ChatState.Goodbye -> VideoConfig("goodbye".lowercase(), shouldLoop = false)
    is ChatState.Fallback -> VideoConfig("fallback".lowercase(), shouldLoop = false)
}

fun toastMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}