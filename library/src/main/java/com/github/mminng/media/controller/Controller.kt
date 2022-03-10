package com.github.mminng.media.controller

import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.renderer.RenderMode

/**
 * Created by zh on 2021/10/1.
 */
interface Controller {

    fun onDuration(duration: Int)

    fun onCurrentPosition(position: Int)

    fun onBufferPosition(position: Int)

    fun onPlayingChanged(isPlaying: Boolean)

    fun onScreenChanged(isFullScreen: Boolean)

    fun onPlayerStateChanged(state: PlayerState, errorMessage: String = "none")

    fun onCompletion()

    fun onPlayerError(errorMessage: String)

    fun onShowController()

    fun onHideController()

    fun changeRenderMode(renderMode: RenderMode)

    fun seekTo(position: Int)

    fun playerBack()

    fun playOrPause(pauseFromUser: Boolean = false)

    fun screenChanged()

    fun changeSpeed(speed: Float)

    fun prepare(playWhenPrepared: Boolean = false)

    fun replay()

    fun retry()

    @LayoutRes
    fun setSwipeProgressView(): Int

    @LayoutRes
    fun setSwipeBrightnessView(): Int

    @LayoutRes
    fun setSwipeVolumeView(): Int

    @LayoutRes
    fun setTouchSpeedView(): Int

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

    fun setControllerReadyListener(listener: () -> Unit)

    fun isControllerReady(): Boolean

    fun onCanBack(): Boolean

    fun getPlayerState(): PlayerState

    fun getView(): View

    fun release()

    fun setListener(listener: Listener)

    interface Listener {

        fun onPrepare(playWhenPrepared: Boolean = false)

        fun onPlayOrPause(pauseFromUser: Boolean = false)

        fun onScreenChanged()

        fun onPlayerBack()

        fun onChangeSpeed(speed: Float)

        fun onTouchSpeed(isTouch: Boolean)

        fun onChangeRenderMode(renderMode: RenderMode)

        fun onSeekTo(position: Int)

        fun onPositionUpdated()

        fun onReplay()

        fun onRetry()

        fun requirePlayerState(): PlayerState

        fun requireCurrentPosition(): Int

        fun requireDuration(): Int
    }

}