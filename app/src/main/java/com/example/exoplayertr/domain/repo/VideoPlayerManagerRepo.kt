package com.example.exoplayertr.domain.repo

import androidx.media3.exoplayer.ExoPlayer

interface VideoPlayerManagerRepo {

    fun getOrCreatePlayer(): ExoPlayer

    fun playVideo(
        videoFileName: String,
        shouldLoop: Boolean,
        nextVideoFileName: String? = null,
        onCompleted: (() -> Unit)? = null
    )

//    fun switchToNextVideo(shouldLoop: Boolean, onCompleted: (() -> Unit)? = null)

    fun pause()

    fun resume()

    fun release()
}