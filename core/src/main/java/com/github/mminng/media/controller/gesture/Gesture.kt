package com.github.mminng.media.controller.gesture

import android.view.View
import com.github.mminng.media.player.PlayerState

/**
 * Created by zh on 2022/1/31.
 */
interface Gesture {

    fun getGestureEnable(): Boolean

    fun setGestureEnable(enable: Boolean)

    fun getView(): View

    fun setListener(listener: Listener)

    interface Listener {

        fun onSingleTap()

        fun onDoubleTap()

        fun onLongTap(isTouch: Boolean)

        fun onSwipeSeekView(
            isShow: Boolean,
            position: Long,
            duration: Long,
            allowSeek: Boolean = false
        )

        fun onSwipeBrightnessView(
            isShow: Boolean,
            position: Int,
            max: Int = 100
        )

        fun onSwipeVolumeView(
            isShow: Boolean,
            position: Int,
            max: Int
        )

        fun getPlayerState(): PlayerState

        fun getPosition(): Long

        fun getDuration(): Long
    }
}