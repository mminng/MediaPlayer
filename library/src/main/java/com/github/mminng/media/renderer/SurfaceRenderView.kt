package com.github.mminng.media.renderer

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.abs

/**
 * Created by zh on 2021/12/4.
 */
class SurfaceRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), Renderer, SurfaceHolder.Callback {

    private var _aspectRatio: Float = 0.0F
    private var _renderMode: RenderMode = RenderMode.FIT
    private var _renderCallback: Renderer.OnRenderCallback? = null

    init {
        holder.addCallback(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (_aspectRatio <= 0) return
        var width: Float = measuredWidth.toFloat()
        var height: Float = measuredHeight.toFloat()
        val currentAspectRatio: Float = width / height
        val difference: Float = _aspectRatio / currentAspectRatio - 1
        if (abs(difference) <= 0.01F) return
        when (_renderMode) {
            RenderMode.FIT -> {
                if (difference > 0) {
                    height = width / _aspectRatio
                } else {
                    width = height * _aspectRatio
                }
            }
            RenderMode.ZOOM -> {
                if (difference > 0) {
                    width = height * _aspectRatio
                } else {
                    height = width / _aspectRatio
                }
            }
            RenderMode.FILL -> {
                //do nothing
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

    override fun setAspectRatio(aspectRatio: Float) {
        if (_aspectRatio != aspectRatio) {
            _aspectRatio = aspectRatio
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