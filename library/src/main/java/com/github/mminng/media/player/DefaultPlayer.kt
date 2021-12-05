package com.github.mminng.media.player

import android.media.MediaPlayer
import android.view.Surface
import android.view.SurfaceHolder

/**
 * Created by zh on 2021/10/2.
 */
class DefaultPlayer : Player {

    private var playerListener: Player.OnPlayerListener? = null
    private val player: MediaPlayer = MediaPlayer()

    init {
        player.setOnPreparedListener {
        }
        player.setOnVideoSizeChangedListener { _, width, height ->
            playerListener?.onVideoSizeChanged(width, height)
        }
        player.setOnCompletionListener {
            playerListener?.onPlayerState(isPlaying())
        }
    }

    override fun setDataSource(source: String) {
        player.setDataSource(source)
    }

    override fun prepareAsync() {
        player.prepareAsync()
    }

    override fun start() {
        player.start()
        playerListener?.onPlayerState(isPlaying())
    }

    override fun pause() {
        player.pause()
        playerListener?.onPlayerState(isPlaying())
    }

    override fun seekTo(position: Int) {
        player.seekTo(position)
    }

    override fun getCurrentPosition(): Int = player.currentPosition

    override fun getDuration(): Int = player.duration

    override fun setDisplay(display: SurfaceHolder) {
        player.setDisplay(display)
    }

    override fun setSurface(display: Surface) {
        player.setSurface(display)
    }

    override fun setOnVideoSizeChangedListener(listener: Player.OnPlayerListener) {
        if (playerListener === listener) return
        playerListener = listener
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun release() {
        player.release()
    }
}