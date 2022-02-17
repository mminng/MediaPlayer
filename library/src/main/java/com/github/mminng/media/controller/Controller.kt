package com.github.mminng.media.controller

import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.player.PlayerState

/**
 * Created by zh on 2021/10/1.
 */
interface Controller {

    fun onPlayOrPause(isPlaying: Boolean)

    fun onDuration(duration: Int)

    fun onCurrentPosition(position: Int)

    fun onBufferPosition(position: Int)

    fun onFullScreenChanged(isFullScreen: Boolean)

    fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

    fun onPlayerError(errorMessage: String)

    fun onShowController()

    fun onHideController()

    @LayoutRes
    fun setCoverView(): Int

    @LayoutRes
    fun setBufferView(): Int

    @LayoutRes
    fun setCompletionView(): Int

    @LayoutRes
    fun setErrorView(): Int

    fun bindCoverImage(view: ImageView)

    fun updatePosition()

    fun stopUpdatePosition()

    fun isControllerReady(): Boolean

    fun getView(): View

    fun release()

    fun setListener(listener: Listener)

    interface Listener {

        fun onPrepare(playWhenPrepared: Boolean = false)

        fun onPlayOrPause(pauseFromUser: Boolean = false)

        fun onFullScreenChanged()

        fun onSeekTo(position: Int)

        fun onPositionUpdated()

        fun onReplay()

        fun onRetry()
    }

}