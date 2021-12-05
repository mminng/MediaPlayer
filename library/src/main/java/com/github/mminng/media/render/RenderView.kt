package com.github.mminng.media.render

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * Created by zh on 2021/12/4.
 */
open class RenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Render {

    private var _aspectRatio: Float = 0.0F
    private var _renderMode: RenderMode = RenderMode.FIT
    var renderCallback: Render.RenderCallback? = null

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

    override fun setCallback(callback: Render.RenderCallback) {
        if (renderCallback === callback) return
        renderCallback = callback
    }
}