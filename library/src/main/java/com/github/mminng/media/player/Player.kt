package com.github.mminng.media.player

import android.view.Surface
import android.view.SurfaceHolder

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

    fun setDisplay(display: SurfaceHolder)

    fun setSurface(display: Surface)

    fun setOnVideoSizeChangedListener(listener: OnPlayerListener)

    fun isPlaying(): Boolean

    fun release()

    interface OnPlayerListener {

        fun onVideoSizeChanged(width: Int, height: Int)

        fun onPlayerState(isPlaying: Boolean)
    }
}