package com.github.mminng.media.exoplayer

import android.content.Context
import android.view.Surface
import com.github.mminng.media.player.BasePlayer
import com.github.mminng.media.player.PlayerState
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.video.VideoSize

/**
 * Created by zh on 2022/1/19.
 */
class DefaultExoPlayer constructor(context: Context) : BasePlayer() {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private var _seekOnCompletionAfter: Boolean = false

    private val listener: Player.Listener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    stateBufferingEnd()
                    if (getPlayerState() == PlayerState.PREPARING) {
                        statePrepared()
                    }
                }
                Player.STATE_BUFFERING -> {
                    stateBufferingStart()
                }
                Player.STATE_ENDED -> {
                    player.playWhenReady = false
                    stateCompletion()
                }
                else -> {
                }
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            videoSizeChanged(videoSize.width, videoSize.height)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            val cause: Throwable? = error.cause
            cause?.message?.let {
                stateError(it)
            }
        }

    }

    init {
        player.playWhenReady = false
        player.addListener(listener)
    }

    override fun setDataSource(source: String) {
        val mediaItem: MediaItem = MediaItem.fromUri(source)
        player.setMediaItem(mediaItem)
    }

    override fun prepare() {
        player.prepare()
    }

    override fun pause() {
        player.pause()
    }

    override fun start() {
        if (getPlayerState() == PlayerState.COMPLETION && !_seekOnCompletionAfter) {
            seekTo(Long.MIN_VALUE + 1)
        }
        player.play()
    }

    override fun seekTo(position: Long) {
        if (getPlayerState() == PlayerState.COMPLETION) {
            _seekOnCompletionAfter = true
        }
        player.seekTo(position.toLong())
    }

    override fun setSurface(surface: Surface?) {
        player.setVideoSurface(surface)
    }

    override fun setSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
    }

    override fun reset() {
        player.stop()
    }

    override fun release() {
        player.release()
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    override fun getPosition(): Long {
        return player.currentPosition
    }

    override fun getDuration(): Long {
        return player.duration
    }

    override fun getVideoWidth(): Int {
        return player.videoSize.width
    }

    override fun getVideoHeight(): Int {
        return player.videoSize.height
    }

    override fun getBufferedPosition(): Long {
        return player.bufferedPosition
    }

    override fun getSpeed(): Float {
        return player.playbackParameters.speed
    }
}