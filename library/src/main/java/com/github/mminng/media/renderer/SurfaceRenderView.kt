package com.github.mminng.media.renderer

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.abs
import kotlin.math.min

/**
 * Created by zh on 2021/12/4.
 */
class SurfaceRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), Renderer, SurfaceHolder.Callback {

    private var _videoWidth: Float = 0.0F
    private var _videoHeight: Float = 0.0F
    private var _renderMode: RenderMode = RenderMode.FIT
    private var _renderCallback: Renderer.OnRenderCallback? = null

    init {
        holder.addCallback(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (_videoWidth == 0.0F || _videoHeight == 0.0F) return
        var width: Float = measuredWidth.toFloat()
        var height: Float = measuredHeight.toFloat()
        val viewAspectRatio: Float = width / height
        val videoAspectRatio: Float = _videoWidth / _videoHeight
        val difference: Float = videoAspectRatio / viewAspectRatio - 1
        if (abs(difference) <= 0.01F) return
        val needBeWider: Boolean = videoAspectRatio > viewAspectRatio
        when (_renderMode) {
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
                    width = min(_videoWidth, width)
                    height = width / videoAspectRatio
                } else {
                    height = min(_videoHeight, height)
                    width = height * videoAspectRatio
                }
            }
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun setRenderMode(renderMode: RenderMode) {
        if (_renderMode != renderMode) {
            _renderMode = renderMode
            requestLayout()
        }
    }

    override fun setVideoSize(width: Float, height: Float) {
        if (_videoWidth != width || _videoHeight != height) {
            _videoWidth = width
            _videoHeight = height
            requestLayout()
        }
    }

    override fun getView(): View = this

    override fun release() {
        //NO OP
    }

    override fun setCallback(callback: Renderer.OnRenderCallback) {
        if (_renderCallback === callback) return
        _renderCallback = callback
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        _renderCallback?.onRenderCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        _renderCallback?.onRenderChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        _renderCallback?.onRenderDestroyed()
    }

}