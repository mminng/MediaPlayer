package com.github.mminng.media.renderer

import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

/**
 * Created by zh on 2021/12/4.
 */
class SurfaceRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), Renderer, SurfaceHolder.Callback {

    private var _videoWidth: Int = 0
    private var _videoHeight: Int = 0
    private var _renderMode: RenderMode = RenderMode.FIT
    private var _listener: Renderer.Listener? = null

    init {
        holder.addCallback(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size: Pair<Int, Int> = resize(
            _videoWidth.toFloat(),
            _videoHeight.toFloat(),
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            _renderMode
        )
        if (size.first == 0 || size.second == 0) return
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size.first, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size.second, MeasureSpec.EXACTLY)
        )
    }

    override fun setRenderMode(renderMode: RenderMode) {
        if (_renderMode != renderMode) {
            _renderMode = renderMode
            requestLayout()
        }
    }

    override fun setVideoSize(width: Int, height: Int) {
        if (_videoWidth != width || _videoHeight != height) {
            _videoWidth = width
            _videoHeight = height
            requestLayout()
        }
    }

    override fun setListener(listener: Renderer.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

    override fun getView(): View = this

    override fun release() {
        holder.removeCallback(this)
        _listener = null
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        _listener?.onRendererCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        _listener?.onRendererChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        _listener?.onRendererDestroyed()
    }
}