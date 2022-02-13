package com.github.mminng.media.controller.gesture

import android.view.View

/**
 * Created by zh on 2022/1/31.
 */
interface Gesture {

    fun getView(): View

    fun setOnGestureListener(listener: OnGestureListener)

    interface OnGestureListener {

        fun onSingleTap()

        fun onDoubleTap()

        fun onLongPress()
    }
}