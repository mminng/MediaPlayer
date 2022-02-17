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

    fun seekTo(position: Int)

    fun setSurface(surface: Surface?)

    fun reset()

    fun release()

    fun isPlaying(): Boolean

    fun getCurrentPosition(): Int

    fun getBufferPosition(): Int

    fun getDuration(): Int

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int

    fun getPlayerState(): PlayerState

    fun setListener(listener: Listener)

    interface Listener {

        fun onVideoSizeChanged(width: Int, height: Int)

        fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

        fun onPlayerState(): PlayerState
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

    fun stateError(errorMessage: String)

}