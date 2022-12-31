package com.github.mminng.media.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View

/**
 * Created by zh on 2021/12/4.
 */
class TextureRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextureView(context, attrs), Renderer, TextureView.SurfaceTextureListener {

    private var _videoWidth: Int = 0
    private var _videoHeight: Int = 0
    private var _renderMode: RenderMode = RenderMode.FIT
    private var _listener: Renderer.Listener? = null
    private var _surfaceTexture: SurfaceTexture? = null
    private val surface: Surface by lazy(LazyThreadSafetyMode.NONE) {
        Surface(surfaceTexture)
    }

    init {
        surfaceTextureListener = this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size: android.util.Pair<Int, Int> = resize(
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
        surface.release()
        _surfaceTexture?.release()
        surfaceTextureListener = null
        _listener = null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        _surfaceTexture?.let {
            setSurfaceTexture(it)
        }
        _listener?.onRendererCreated(this.surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        _listener?.onRendererChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        _surfaceTexture = surface
        _listener?.onRendererDestroyed()
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        //NO OP
    }
}