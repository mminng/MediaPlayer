package com.github.mminng.media.controller.gesture

import android.view.View

/**
 * Created by zh on 2022/1/31.
 */
interface Gesture {

    fun getView(): View

    fun setListener(listener: Listener)

    interface Listener {

        fun onSingleTap()

        fun onDoubleTap()

        fun onLongTap(isTouch: Boolean)
    }
}