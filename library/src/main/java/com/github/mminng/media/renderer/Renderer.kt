package com.github.mminng.media.renderer

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

    fun setCallback(callback: OnRenderCallback)

    interface OnRenderCallback {

        fun onRenderCreated(surface: Surface)

        fun onRenderChanged(width: Int, height: Int)

        fun onRenderDestroyed()
    }

    fun resize(
        videoWidth: Float,
        videoHeight: Float,
        measuredWidth: Float,
        measuredHeight: Float,
        renderMode: RenderMode
    ): IntArray {
        if (videoWidth == 0.0F || videoHeight == 0.0F) return intArrayOf()
        var width: Float = measuredWidth
        var height: Float = measuredHeight
        val viewAspectRatio: Float = width / height
        val videoAspectRatio: Float = videoWidth / videoHeight
        val difference: Float = videoAspectRatio / viewAspectRatio - 1
        if (abs(difference) <= 0.01F) return intArrayOf()
        val needBeWider: Boolean = videoAspectRatio > viewAspectRatio
        when (renderMode) {
            RenderMode.FIT -> {
                if (difference > 0) {
                    height = width / videoAspectRatio
                } else {
                    width = height * videoAspectRatio
                }
            }
            RenderMode.FILL -> {
                //do nothing
            }
            RenderMode.ZOOM -> {
                if (difference > 0) {
                    width = height * videoAspectRatio
                } else {
                    height = width / videoAspectRatio
                }
            }
            RenderMode.DEFAULT -> {
                if (needBeWider) {
                    width = min(videoWidth, width)
                    height = width / videoAspectRatio
                } else {
                    height = min(videoHeight, height)
                    width = height * videoAspectRatio
                }
            }
        }
        return intArrayOf(width.toInt(), height.toInt())
    }

}