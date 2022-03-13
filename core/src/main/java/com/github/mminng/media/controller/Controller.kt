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

    fun onDuration(duration: Long)

    fun onPosition(position: Long)

    fun onBufferedPosition(bufferedPosition: Long)

    fun onPlayingChanged(isPlaying: Boolean)

    fun onScreenChanged(isFullScreen: Boolean)

    fun onPlayerStateChanged(state: PlayerState, error: String = "none")

    fun onCompletion()

    fun onPlayerError(error: String)

    fun onShowController()

    fun onHideController()

    fun requireCover(): ImageView

    fun playerBack()

    fun prepare(playWhenPrepared: Boolean = false)

    fun playOrPause(pauseFromUser: Boolean = false)

    fun seekTo(position: Long)

    fun changeSpeed(speed: Float)

    fun screenChanged()

    fun changeRenderMode(renderMode: RenderMode)

    fun replay()

    fun retry()

    fun setReadyListener(listener: () -> Unit)

    fun isReady(): Boolean

    fun canBack(): Boolean

    fun getPlayerState(): PlayerState

    fun getView(): View

    fun updatePosition()

    fun stopUpdatePosition()

    fun release()

    fun setListener(listener: Listener)

    @LayoutRes
    fun setCoverLayout(): Int

    @LayoutRes
    fun setBufferingLayout(): Int

    @LayoutRes
    fun setCompletionLayout(): Int

    @LayoutRes
    fun setErrorLayout(): Int

    @LayoutRes
    fun setSwipeSeekLayout(): Int

    @LayoutRes
    fun setSwipeBrightnessLayout(): Int

    @LayoutRes
    fun setSwipeVolumeLayout(): Int

    @LayoutRes
    fun setTouchSpeedLayout(): Int

    interface Listener {

        fun onPlayerBack()

        fun onPrepare(playWhenPrepared: Boolean = false)

        fun onPlayOrPause(pauseFromUser: Boolean = false)

        fun onSeekTo(position: Long)

        fun onChangeSpeed(speed: Float)

        fun onTouchSpeed(isTouch: Boolean)

        fun onScreenChanged()

        fun onChangeRenderMode(renderMode: RenderMode)

        fun onReplay()

        fun onRetry()

        fun onPosition()

        fun requirePlayerState(): PlayerState

        fun requirePosition(): Long

        fun requireDuration(): Long
    }
}