package com.github.mminng.media.controller

import android.view.View

/**
 * Created by zh on 2021/10/1.
 */
interface Controller {

    fun onPlayPause(isPlaying: Boolean)

    fun onFullScreen(isFullScreen: Boolean)

    fun onDuration(duration: Int)

    fun onProgressUpdate(progress: Int)

    fun onBufferingProgressUpdate(bufferingProgress: Int)

    fun getView(): View

    fun updateProgress()

    fun stopProgress()

    fun setOnControllerListener(listener: OnControllerListener)

    interface OnControllerListener {

        fun onPlayPause()

        fun onFullScreen()

        fun onSeekTo(position: Int)

        fun onProgressUpdate()
    }

}