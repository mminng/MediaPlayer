package com.github.mminng.media.controller

import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.controller.gesture.Gesture
import com.github.mminng.media.player.PlayerState

/**
 * Created by zh on 2021/10/1.
 */
interface Controller {

    fun onPlayOrPause(isPlaying: Boolean)

    fun onDuration(duration: Int)

    fun onCurrentPosition(position: Int)

    fun onBufferingPosition(position: Int)

    fun onFullScreen(isFullScreen: Boolean)

    fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

    fun onPlayerError(errorMessage: String)

    fun onShowController()

    fun onHideController()

    @LayoutRes
    fun setCoverView(): Int

    @LayoutRes
    fun setBufferingView(): Int

    @LayoutRes
    fun setCompletionView(): Int

    @LayoutRes
    fun setErrorView(): Int

    fun setGestureController(gestureController: Gesture)

    fun bindCoverImage(view: ImageView)

    fun setCoverViewEnable(enable: Boolean)

    fun setCompletionViewEnable(enable: Boolean)

    fun setErrorViewEnable(enable: Boolean)

    fun updatePosition()

    fun stopUpdatePosition()

    fun isControllerReady(): Boolean

    fun getView(): View

    fun release()

    fun setOnControllerListener(listener: OnControllerListener)

    interface OnControllerListener {

        fun prepare(playWhenPrepared: Boolean = false)

        fun onPlayOrPause(pauseFromUser: Boolean = false)

        fun onFullScreen()

        fun onSeekTo(position: Int)

        fun onPositionUpdated()

        fun onReplay()

        fun onRetry()
    }

}