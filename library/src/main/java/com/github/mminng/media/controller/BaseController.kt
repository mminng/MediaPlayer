package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.github.mminng.media.R
import com.github.mminng.media.player.state.PlayerState

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
        inflateLayout(setControllerLayout(), true)
    }
    private val stateCoverView: View by lazy {
        inflateLayout(setCoverView())
    }
    private val stateBufferingView: View by lazy {
        inflateLayout(setBufferingView())
    }
    private val stateCompletionView: View by lazy {
        inflateLayout(setCompletionView())
    }
    private val stateErrorView: View by lazy {
        inflateLayout(setErrorView())
    }
    private val progressRunnable: Runnable = Runnable {
        updateProgress()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onLayoutCreated(layout)
        addView(stateBufferingView, 0)
        addView(stateCoverView)
        addView(stateCompletionView)
        addView(stateErrorView)
        stateCompletionView.visibility = INVISIBLE
        stateErrorView.visibility = INVISIBLE
    }

    @LayoutRes
    abstract fun setControllerLayout(): Int

    abstract fun onLayoutCreated(view: View)

    override fun setCoverView(): Int {
        return R.layout.default_state_cover_layout
    }

    override fun setBufferingView(): Int {
        return R.layout.default_state_buffering_layout
    }

    override fun setCompletionView(): Int {
        return R.layout.default_state_completion_layout
    }

    override fun setErrorView(): Int {
        return R.layout.default_state_error_layout
    }

    override fun getView(): View = this

    override fun updateProgress() {
        controllerListener?.onProgressUpdate()
        postDelayed(progressRunnable, UPDATE_INTERVAL)
    }

    override fun stopProgress() {
        removeCallbacks(progressRunnable)
    }

    override fun setStateView(state: PlayerState, errorMessage: String) {
        when (state) {
            PlayerState.IDLE -> {
            }
            PlayerState.BUFFERING -> {
                stateBufferingView.visibility = VISIBLE
            }
            PlayerState.BUFFERED -> {
                stateBufferingView.visibility = INVISIBLE
            }
            PlayerState.PREPARED -> {
            }
            PlayerState.STARTED -> {
            }
            PlayerState.PAUSED -> {
            }
            PlayerState.COMPLETED -> {
                stateCompletionView.visibility = VISIBLE
            }
            PlayerState.ERROR -> {
                stateErrorView.visibility = VISIBLE
            }
        }
    }

    override fun setOnControllerListener(listener: Controller.OnControllerListener) {
        if (controllerListener === listener) return
        controllerListener = listener
    }

    private fun inflateLayout(@LayoutRes layout: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layout, this, attachToRoot)
    }

}