package com.easyfun.mediaplayer

import android.content.Context
import android.util.Log
import android.view.Surface
import com.github.mminng.media.player.BasePlayer
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize

/**
 * Created by zh on 2022/1/19.
 */
class Player constructor(context: Context) : BasePlayer() {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val listener: Player.Listener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    statePrepared()
                }
                Player.STATE_BUFFERING -> {
                    stateBufferingStart()
                }
                Player.STATE_ENDED -> {
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
        player.play()
    }

    override fun seekTo(position: Int) {
        player.seekTo(position.toLong())
    }

    override fun setSurface(surface: Surface?) {
        player.setVideoSurface(surface)
    }

    override fun reset() {
        player.stop()
    }

    override fun release() {
        player.release()
        Log.d("PlayerDebug", "ExoPlayer released")
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    override fun getCurrentPosition(): Int {
        return player.currentPosition.toInt()
    }

    override fun getDuration(): Int {
        return player.duration.toInt()
    }

    override fun getVideoWidth(): Int {
        return player.videoSize.width
    }

    override fun getVideoHeight(): Int {
        return player.videoSize.height
    }

    override fun getBufferingPosition(): Int {
        return player.bufferedPosition.toInt()
    }

}