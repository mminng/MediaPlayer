package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.R
import com.github.mminng.media.player.PlayerState

/**
 * Created by zh on 2021/12/9.
 */
abstract class BaseController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Controller {

    companion object {
        private const val UPDATE_INTERVAL: Long = 500
    }

    private var _coverViewEnable: Boolean = true
    private var _completionViewEnable: Boolean = true
    private var _errorViewEnable: Boolean = true
    private var _controllerIsReady: Boolean = false

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
        updatePosition()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (_controllerIsReady) return
        onLayoutCreated(controllerLayout)
        addView(stateBufferingView, 0)
        addView(stateCompletionView)
        addView(stateErrorView)
        addView(stateCoverView)
        stateBufferingView.visibility = INVISIBLE
        stateCompletionView.visibility = GONE
        stateErrorView.visibility = GONE
        _controllerIsReady = true
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

    override fun bindCoverImage(view: ImageView) {
        controllerListener?.onBindCoverImage(view)
    }

    override fun setCoverViewEnable(enable: Boolean) {
        _coverViewEnable = enable
        if (enable) {
            stateCoverView.visibility = VISIBLE
        } else {
            stateCoverView.visibility = GONE
        }
    }

    override fun setCompletionViewEnable(enable: Boolean) {
        _completionViewEnable = enable
        if (enable) {
            stateCompletionView.visibility = VISIBLE
        } else {
            stateCompletionView.visibility = GONE
        }
    }

    override fun setErrorViewEnable(enable: Boolean) {
        _errorViewEnable = enable
        if (enable) {
            stateErrorView.visibility = VISIBLE
        } else {
            stateErrorView.visibility = GONE
        }
    }

    override fun getView(): View = this

    override fun isControllerReady(): Boolean = _controllerIsReady

    override fun updatePosition() {
        controllerListener?.onPositionUpdated()
        postDelayed(progressRunnable, UPDATE_INTERVAL)
    }

    override fun stopUpdatePosition() {
        removeCallbacks(progressRunnable)
    }

    override fun onPlayerStateChanged(state: PlayerState, errorMessage: String) {
        when (state) {
            PlayerState.IDLE -> {
            }
            PlayerState.INITIALIZED -> {
            }
            PlayerState.PREPARING -> {
                stateBufferingView.visibility = VISIBLE
            }
            PlayerState.PREPARED -> {
                stateBufferingView.visibility = INVISIBLE
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
                if (_completionViewEnable) {
                    stateCompletionView.visibility = VISIBLE
                }
            }
            PlayerState.ERROR -> {
                if (_errorViewEnable) {
                    stateErrorView.visibility = VISIBLE
                }
                onPlayerError(errorMessage)
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

}