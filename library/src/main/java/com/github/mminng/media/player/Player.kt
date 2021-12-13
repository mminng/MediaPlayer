package com.github.mminng.media.player

import android.view.Surface
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

    fun setSurface(surface: Surface)

    fun reset()

    fun release()

    fun isPlaying(): Boolean

    fun getCurrentPosition(): Int

    fun getDuration(): Int

    fun getPlayerState(): PlayerState

    fun setOnPlayerListener(listener: OnPlayerListener)

    fun setOnPlayerStateListener(listener: OnPlayerStateListener)

    interface OnPlayerListener {

        fun onVideoSizeChanged(width: Int, height: Int)

        fun onBufferingUpdate(bufferingProgress: Int)
    }

    fun videoSizeChanged(width: Int, height: Int)

    fun bufferingUpdate(bufferingProgress: Int)

    interface OnPlayerStateListener {

        fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "")

        fun getPlayerState(): PlayerState
    }

    fun prepared()

    fun bufferingStart()

    fun bufferingEnd()

    fun renderingStart()

    fun completion()

    fun error(errorMessage: String)

}