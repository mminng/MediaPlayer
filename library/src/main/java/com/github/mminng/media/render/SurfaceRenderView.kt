package com.github.mminng.media.render

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Created by zh on 2021/12/4.
 */
class SurfaceRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RenderView(context, attrs), SurfaceHolder.Callback {

    init {
        val surfaceView = SurfaceView(context)
        addView(surfaceView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        renderCallback?.onRenderCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        renderCallback?.onRenderChanged(holder.surface, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderCallback?.onRenderDestroyed()
    }
}