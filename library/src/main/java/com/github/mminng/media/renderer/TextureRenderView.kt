package com.github.mminng.media.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.github.mminng.media.utils.d
import kotlin.math.abs
import kotlin.math.min

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
        _surfaceTexture?.release()
        surface.release()
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