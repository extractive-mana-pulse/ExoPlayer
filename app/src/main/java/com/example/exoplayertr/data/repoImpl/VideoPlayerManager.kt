package com.example.exoplayertr.data.repoImpl

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayertr.domain.repo.VideoPlayerManagerRepo

@OptIn(UnstableApi::class)
class VideoPlayerManager(
    private val context: Context
) : VideoPlayerManagerRepo {

    private var currentPlayer: ExoPlayer? = null
    private var nextPlayer: ExoPlayer? = null
    private var onVideoCompleted: (() -> Unit)? = null

    override fun getOrCreatePlayer(): ExoPlayer {
        if (currentPlayer == null) {
            currentPlayer = createPlayer()
        }
        return currentPlayer!!
    }

    override fun playVideo(
        videoFileName: String,
        shouldLoop: Boolean,
        nextVideoFileName: String?,
        onCompleted: (() -> Unit)?
    ) {
        this.onVideoCompleted = onCompleted

        val exoPlayer = getOrCreatePlayer()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        val currentItem = createMediaItem(videoFileName)
        exoPlayer.addMediaItem(currentItem)

        nextVideoFileName?.let {
            val nextItem = createMediaItem(it)
            exoPlayer.addMediaItem(nextItem)
        }

        exoPlayer.repeatMode =
            if (shouldLoop) Player.REPEAT_MODE_ONE
            else Player.REPEAT_MODE_OFF

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        if (repeatMode != Player.REPEAT_MODE_ONE) {
                            onVideoCompleted?.invoke()
                        }
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                        currentMediaItemIndex == 1 &&
                        repeatMode != Player.REPEAT_MODE_ONE) {
                        onVideoCompleted?.invoke()
                    }
                }
            })
        }
    }

    private fun createMediaItem(videoFileName: String): MediaItem {
        val resourceId = context.resources.getIdentifier(
            videoFileName,
            "raw",
            context.packageName
        )

        if (resourceId == 0) {
            throw IllegalArgumentException("Video not found: $videoFileName")
        }

        val uri = RawResourceDataSource.buildRawResourceUri(resourceId)
        return MediaItem.fromUri(uri)
    }

    override fun pause() {
        currentPlayer?.pause()
    }

    override fun resume() {
        currentPlayer?.play()
    }

    override fun release() {
        currentPlayer?.release()
        nextPlayer?.release()
        currentPlayer = null
        nextPlayer = null
        onVideoCompleted = null
    }
}