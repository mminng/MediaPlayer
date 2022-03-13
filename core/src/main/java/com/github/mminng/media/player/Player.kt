package com.github.mminng.media.player

import android.view.Surface

/**
 * Created by zh on 2021/9/20.
 */
interface Player {

    fun setDataSource(source: String)

    fun prepare()

    fun start()

    fun pause()

    fun seekTo(position: Long)

    fun setSurface(surface: Surface?)

    fun setSpeed(speed: Float)

    fun reset()

    fun release()

    fun isPlaying(): Boolean

    fun getPosition(): Long

    fun getBufferedPosition(): Long

    fun getDuration(): Long

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int

    fun getSpeed(): Float

    fun getPlayerState(): PlayerState

    fun setListener(listener: Listener)

    interface Listener {

        fun onVideoSizeChanged(width: Int, height: Int)

        fun onPlayerStateChanged(state: PlayerState, error: String = "none")

        fun requirePlayerState(): PlayerState
    }

    fun videoSizeChanged(width: Int, height: Int)

    fun stateIdle()

    fun stateInitialized()

    fun statePreparing()

    fun statePrepared()

    fun stateBufferingStart()

    fun stateBufferingEnd()

    fun stateRenderingStart()

    fun stateStarted()

    fun statePaused()

    fun stateCompletion()

    fun stateError(error: String)
}