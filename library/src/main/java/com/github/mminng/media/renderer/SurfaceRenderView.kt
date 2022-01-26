package com.github.mminng.media.renderer

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

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
        val size: IntArray =
            resize(_videoWidth, _videoHeight, measuredWidth, measuredHeight, _renderMode)
        if (size.isEmpty()) return
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size[0], MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size[1], MeasureSpec.EXACTLY)
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
        holder.removeCallback(this)
        _renderCallback = null
    }

    override fun setCallback(callback: Renderer.OnRenderCallback) {
        if (_renderCallback === callback) return
        _renderCallback = callback
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        _renderCallback?.onRenderCreated(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        _renderCallback?.onRenderChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        _renderCallback?.onRenderDestroyed()
    }

}