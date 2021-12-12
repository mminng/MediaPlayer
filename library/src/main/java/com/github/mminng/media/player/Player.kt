package com.github.mminng.media.player

import android.view.Surface
import android.view.SurfaceHolder
import com.github.mminng.media.player.state.PlayerState

/**
 * Created by zh on 2021/9/20.
 */
interface Player {

    fun setDataSource(source: String)

    fun prepareAsync()

    fun start()

    fun pause()

    fun seekTo(position: Int)

    fun getCurrentPosition(): Int

    fun getDuration(): Int

    fun videoSizeChanged(width: Int, height: Int)

    fun bufferingUpdate(bufferingProgress: Int)

    fun playerStateChanged(state: PlayerState)

    fun setDisplay(display: SurfaceHolder)

    fun setSurface(display: Surface)

    fun isPlaying(): Boolean

    fun completion()

    fun prepared()

    fun bufferingStart()

    fun bufferingEnd()

    fun error(message: String)

    fun release()

    fun setOnPlayerListener(listener: OnPlayerListener)

    fun setOnPlayerStateListener(listener: OnPlayerStateListener)

    interface OnPlayerListener {

        fun onVideoSizeChanged(width: Int, height: Int)

        fun onBufferingUpdate(bufferingProgress: Int)
    }

    interface OnPlayerStateListener {

        fun onPlayerStateChanged(state: PlayerState, error: String = "")
    }

}