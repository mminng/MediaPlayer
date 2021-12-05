package com.github.mminng.media.render

import android.view.Surface

/**
 * Created by zh on 2021/12/4.
 */
interface Render {

    fun setRenderMode(renderMode: RenderMode)

    fun setAspectRatio(aspectRatio: Float)

    fun setCallback(callback: RenderCallback)

    interface RenderCallback {

        fun onRenderCreated(surface: Surface)

        fun onRenderChanged(surface: Surface, width: Int, height: Int)

        fun onRenderDestroyed()
    }
}