package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes

/**
 * Created by zh on 2021/12/9.
 */
abstract class BaseController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Controller {

    companion object {
        private const val UPDATE_INTERVAL: Long = 300
    }

    var controllerListener: Controller.OnControllerListener? = null
    private val layout: View by lazy {
        LayoutInflater.from(context).inflate(setControllerLayout(), this)
    }
    private val progressRunnable: Runnable = Runnable {
        updateProgress()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onLayoutCreated(layout)
    }

    @LayoutRes
    abstract fun setControllerLayout(): Int

    abstract fun onLayoutCreated(view: View)

    override fun getView(): View = this

    override fun updateProgress() {
        controllerListener?.onProgressUpdate()
        postDelayed(progressRunnable, UPDATE_INTERVAL)
    }

    override fun stopProgress() {
        removeCallbacks(progressRunnable)
    }

    override fun setOnControllerListener(listener: Controller.OnControllerListener) {
        if (controllerListener === listener) return
        controllerListener = listener
    }

}