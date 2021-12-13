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

    private var errorMessage: String = ""

    var controllerListener: Controller.OnControllerListener? = null
    private val controllerLayout: View by lazy {
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
        onLayoutCreated(controllerLayout)
        addView(stateBufferingView, 0)
        addView(stateCompletionView)
        addView(stateErrorView)
        addView(stateCoverView)
        stateBufferingView.visibility = INVISIBLE
        stateCompletionView.visibility = GONE
        stateErrorView.visibility = GONE
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
        stopProgress()
        controllerListener?.onProgressUpdate()
        postDelayed(progressRunnable, UPDATE_INTERVAL)
    }

    override fun stopProgress() {
        removeCallbacks(progressRunnable)
    }

    override fun setControllerState(state: PlayerState, errorMessage: String) {
        when (state) {
            PlayerState.IDLE -> {
            }
            PlayerState.INITIALIZED -> {
            }
            PlayerState.PREPARING -> {
            }
            PlayerState.PREPARED -> {
            }
            PlayerState.BUFFERING -> {
                stateBufferingView.visibility = VISIBLE
            }
            PlayerState.BUFFERED -> {
                stateBufferingView.visibility = INVISIBLE
            }
            PlayerState.RENDERING -> {
            }
            PlayerState.STARTED -> {
            }
            PlayerState.PAUSED -> {
            }
            PlayerState.COMPLETION -> {
                stateCompletionView.visibility = VISIBLE
            }
            PlayerState.ERROR -> {
                stateErrorView.visibility = VISIBLE
                this.errorMessage = errorMessage
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

    fun getCoverView(): View = stateCoverView

    fun getCompletionView(): View = stateCompletionView

    fun getErrorView(): View = stateErrorView

    fun getErrorMessage(): String = errorMessage

}