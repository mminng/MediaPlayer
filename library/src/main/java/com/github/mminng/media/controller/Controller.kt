package com.github.mminng.media.controller

/**
 * Created by zh on 2021/10/1.
 */
interface Controller {

    fun onPlayPause(isPlaying: Boolean)

    fun onFullScreen(isFullScreen: Boolean)

    fun onDuration(duration: Int)

    fun onProgress(progress: Int)

    fun setOnControllerListener(listener: OnControllerListener)

    interface OnControllerListener {

        fun onPlayPause()

        fun onFullScreen()

        fun onProgressChanged(position: Int)
    }
}