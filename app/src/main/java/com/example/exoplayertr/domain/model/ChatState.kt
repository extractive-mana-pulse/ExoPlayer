package com.example.exoplayertr.domain.model

data class VideoConfig(
    val fileName: String,
    val shouldLoop: Boolean = false
)