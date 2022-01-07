package com.github.mminng.media.controller

import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.player.PlayerState

/**
 * Created by zh on 2021/10/1.
 */
interface Controller {

    fun onPlayPause(isPlaying: Boolean)

    fun onDuration(duration: Int)

    fun onCurrentPosition(position: Int)

    fun onCurrentBufferingPosition(bufferingPosition: Int)

    fun onFullScreen(isFullScreen: Boolean)

    fun getView(): View

    fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

    @LayoutRes
    fun setCoverView(): Int

    @LayoutRes
    fun setBufferingView(): Int

    @LayoutRes
    fun setCompletionView(): Int

    @LayoutRes
    fun setErrorView(): Int

    fun bindCoverImage(view: ImageView)

    fun setCoverViewEnable(enable: Boolean)

    fun setCompletionViewEnable(enable: Boolean)

    fun setErrorViewEnable(enable: Boolean)

    fun updatePosition()

    fun stopUpdatePosition()

    fun onPlayerError(errorMessage: String)

    fun isControllerReady(): Boolean

    fun setOnControllerListener(listener: OnControllerListener)

    interface OnControllerListener {

        fun onBindCoverImage(view: ImageView)

        fun prepare(playWhenPrepared: Boolean = false)

        fun onPlayPause(pauseFromUser: Boolean = false)

        fun onFullScreen()

        fun onSeekTo(position: Int)

        fun onPositionUpdated()

        fun onReplay()

        fun onRetry()
    }

}