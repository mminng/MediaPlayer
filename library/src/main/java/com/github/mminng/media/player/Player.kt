package com.github.mminng.media.player

import android.view.Surface

/**
 * Created by zh on 2021/9/20.
 */
interface Player {

    fun setDataSource(source: String)

    fun prepare()

    fun pause()

    fun start()

    fun seekTo(position: Int)

    fun setSurface(surface: Surface?)

    fun reset()

    fun release()

    fun isPlaying(): Boolean

    fun getCurrentPosition(): Int

    fun getBufferingPosition(): Int

    fun getDuration(): Int

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int

    fun getPlayerState(): PlayerState

    fun setOnPlayerListener(listener: OnPlayerListener)

    interface OnPlayerListener {

        fun onVideoSizeChanged(width: Int, height: Int)

        fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

        fun getPlayerState(): PlayerState
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