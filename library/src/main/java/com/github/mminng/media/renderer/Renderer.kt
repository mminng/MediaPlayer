package com.github.mminng.media.renderer

import android.view.Surface
import android.view.View

/**
 * Created by zh on 2021/12/4.
 */
interface Renderer {

    fun setRenderMode(renderMode: RenderMode)

    fun setVideoSize(width: Float, height: Float)

    fun getView(): View

    fun release()

    fun setCallback(callback: OnRenderCallback)

    interface OnRenderCallback {

        fun onRenderCreated(surface: Surface)

        fun onRenderChanged(width: Int, height: Int)

        fun onRenderDestroyed()
    }

}