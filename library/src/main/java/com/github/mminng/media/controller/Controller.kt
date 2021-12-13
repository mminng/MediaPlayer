package com.github.mminng.media.controller

import android.view.View
import androidx.annotation.LayoutRes
import com.github.mminng.media.R
import com.github.mminng.media.player.state.PlayerState

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

    fun setControllerState(state: PlayerState, errorMessage: String = "")

    @LayoutRes
    fun setCoverView(): Int

    @LayoutRes
    fun setBufferingView(): Int

    @LayoutRes
    fun setCompletionView(): Int

    @LayoutRes
    fun setErrorView(): Int

    fun setOnControllerListener(listener: OnControllerListener)

    interface OnControllerListener {

        fun onPrepareAsync()

        fun onPlayPause()

        fun onFullScreen()

        fun onSeekTo(position: Int)

        fun onProgressUpdate()

        fun onReplay()

        fun onRetry()
    }

}