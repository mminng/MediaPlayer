package com.github.mminng.media.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.github.mminng.media.utils.d

/**
 * Created by zh on 2021/12/4.
 */
class TextureRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextureView(context, attrs), Renderer, TextureView.SurfaceTextureListener {

    private var _videoWidth: Float = 0.0F
    private var _videoHeight: Float = 0.0F
    private var _renderMode: RenderMode = RenderMode.FIT
    private var _renderCallback: Renderer.OnRenderCallback? = null
    private var _surfaceTexture: SurfaceTexture? = null
    private val surface: Surface by lazy {
        Surface(surfaceTexture)
    }

    init {
        surfaceTextureListener = this
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
        surface.release()
        _surfaceTexture?.release()
        surfaceTextureListener = null
        _renderCallback = null
        d("TextureRenderView released")
    }

    override fun setCallback(callback: Renderer.OnRenderCallback) {
        if (_renderCallback === callback) return
        _renderCallback = callback
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        _surfaceTexture?.let {
            setSurfaceTexture(it)
        }
        _renderCallback?.onRenderCreated(this.surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        _renderCallback?.onRenderChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        _surfaceTexture = surface
        _renderCallback?.onRenderDestroyed()
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        //NO OP
    }

}