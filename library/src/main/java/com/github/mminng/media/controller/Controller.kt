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

    fun onScreenChanged(isFullScreen: Boolean)

    fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

    fun onCompletion()

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

    fun getCoverView(): View

    fun getCompletionView(): View

    fun getErrorView(): View

    fun bindCoverImage(view: ImageView)

    fun updatePosition()

    fun stopUpdatePosition()

    fun isControllerReady(): Boolean

    fun onCanBack(): Boolean

    fun getView(): View

    fun getPlayerState(): PlayerState

    fun release()

    fun setListener(listener: Listener)

    interface Listener {

        fun onPrepare(playWhenPrepared: Boolean = false)

        fun onPlayOrPause(pauseFromUser: Boolean = false)

        fun onPlayerBack()

        fun onChangeSpeed(speed: Float)

        fun onTouchSpeed(isTouch: Boolean)

        fun onScreenChanged()

        fun onSeekTo(position: Int)

        fun onPositionUpdated()

        fun onReplay()

        fun onRetry()

        fun onPlayerState(): PlayerState
    }

}