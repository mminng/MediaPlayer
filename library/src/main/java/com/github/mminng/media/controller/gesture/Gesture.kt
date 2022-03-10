package com.github.mminng.media.controller.gesture

import android.view.View

/**
 * Created by zh on 2022/1/31.
 */
interface Gesture {

    fun setGestureEnable(enable: Boolean)

    fun getGestureEnable(): Boolean

    fun getView(): View

    fun setListener(listener: Listener)

    interface Listener {

        fun onSingleTap()

        fun onDoubleTap()

        fun onLongTap(isTouch: Boolean)

        fun onSwipeProgressView(
            show: Boolean,
            currentPosition: Int,
            duration: Int,
            canSeek: Boolean = false
        )

        fun onSwipeBrightnessView(show: Boolean, progress: Int, max: Int)

        fun onSwipeVolumeView(show: Boolean, progress: Int, max: Int)

        fun getCurrentPosition(): Int

        fun getDuration(): Int
    }
}