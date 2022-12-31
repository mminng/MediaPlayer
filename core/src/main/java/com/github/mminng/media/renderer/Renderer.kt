package com.github.mminng.media.renderer

import android.util.Pair
import android.view.Surface
import android.view.View
import kotlin.math.abs
import kotlin.math.min

/**
 * Created by zh on 2021/12/4.
 */
interface Renderer {

    fun setRenderMode(renderMode: RenderMode)

    fun setVideoSize(width: Int, height: Int)

    fun getView(): View

    fun release()

    fun setListener(listener: Listener)

    interface Listener {

        fun onRendererCreated(surface: Surface)

        fun onRendererChanged(width: Int, height: Int)

        fun onRendererDestroyed()
    }

    fun resize(
        videoWidth: Float,
        videoHeight: Float,
        measuredWidth: Float,
        measuredHeight: Float,
        renderMode: RenderMode
    ): Pair<Int, Int> {
        if (videoWidth == 0.0F || videoHeight == 0.0F) return Pair.create(0, 0)
        var width: Float = measuredWidth
        var height: Float = measuredHeight
        val viewAspectRatio: Float = width / height
        val videoAspectRatio: Float = videoWidth / videoHeight
        val difference: Float = videoAspectRatio / viewAspectRatio - 1
        if (abs(difference) <= 0.01F) return Pair.create(0, 0)
        when (renderMode) {
            RenderMode.FIT -> {
                if (difference > 0) {
                    height = width / videoAspectRatio
                } else {
                    width = height * videoAspectRatio
                }
            }
            RenderMode.FILL -> {
                //NO OP
            }
            RenderMode.ZOOM -> {
                if (difference > 0) {
                    width = height * videoAspectRatio
                } else {
                    height = width / videoAspectRatio
                }
            }
            RenderMode.DEFAULT -> {
                if (videoAspectRatio > viewAspectRatio) {
                    width = min(videoWidth, width)
                    height = width / videoAspectRatio
                } else {
                    height = min(videoHeight, height)
                    width = height * videoAspectRatio
                }
            }
        }
        return Pair.create(width.toInt(), height.toInt())
    }
}