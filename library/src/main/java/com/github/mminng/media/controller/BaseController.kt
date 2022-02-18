package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.R
import com.github.mminng.media.controller.gesture.GestureController
import com.github.mminng.media.controller.gesture.Gesture
import com.github.mminng.media.player.PlayerState

/**
 * Created by zh on 2021/12/9.
 */
abstract class BaseController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Controller, Gesture.Listener {

    private var _updateInterval: Long = 1000
    private var _visibilityInterval: Long = 5 * 1000
    private var _controllerIsReady: Boolean = false
    private var _controllerIsVisible: Boolean = false
    private var _coverViewEnable: Boolean = false
    private var _completionViewEnable: Boolean = false
    private var _errorViewEnable: Boolean = false
    private var _gestureController: Gesture = GestureController(context)
    private var _onCoverBindListener: ((view: ImageView) -> Unit)? = null
    var controllerListener: Controller.Listener? = null

    private val controllerView: View by lazy {
        inflateLayout(setControllerLayout(), true)
    }
    private val stateCoverView: View by lazy {
        inflateLayout(setCoverView())
    }
    private val stateBufferView: View by lazy {
        inflateLayout(setBufferView())
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
    private val visibilityRunnable: Runnable = Runnable {
        hideController()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (_controllerIsReady) return
        onLayoutCreated(controllerView)
        addView(_gestureController.getView(), 0)
        addView(stateBufferView, 0)
        addView(stateCompletionView)
        addView(stateErrorView)
        addView(stateCoverView)
        stateBufferView.visibility = INVISIBLE
        stateCompletionView.visibility = GONE
        stateErrorView.visibility = GONE
        stateCoverView.visibility = if (_coverViewEnable) VISIBLE else GONE
        _gestureController.setListener(this)
        _controllerIsReady = true
    }

    @LayoutRes
    abstract fun setControllerLayout(): Int

    abstract fun onLayoutCreated(view: View)

    override fun onPlayerStateChanged(state: PlayerState, errorMessage: String) {
        when (state) {
            PlayerState.IDLE -> {
            }
            PlayerState.INITIALIZED -> {
            }
            PlayerState.PREPARING -> {
                stateBufferView.visibility = VISIBLE
            }
            PlayerState.PREPARED -> {
                stateBufferView.visibility = INVISIBLE
            }
            PlayerState.BUFFERING -> {
                stateBufferView.visibility = VISIBLE
            }
            PlayerState.BUFFERED -> {
                stateBufferView.visibility = INVISIBLE
            }
            PlayerState.RENDERING -> {
            }
            PlayerState.STARTED -> {
                showController()
            }
            PlayerState.PAUSED -> {
                showController(false)
            }
            PlayerState.COMPLETION -> {
                if (_completionViewEnable) {
                    stateCompletionView.visibility = VISIBLE
                    hideController()
                } else {
                    showController(false)
                }
            }
            PlayerState.ERROR -> {
                if (_errorViewEnable) {
                    stateErrorView.visibility = VISIBLE
                    hideController()
                } else {
                    showController(false)
                }
                onPlayerError(errorMessage)
            }
        }
    }

    override fun setCoverView(): Int {
        return R.layout.default_state_cover_layout
    }

    override fun setBufferView(): Int {
        return R.layout.default_state_buffer_layout
    }

    override fun setCompletionView(): Int {
        return R.layout.default_state_completion_layout
    }

    override fun setErrorView(): Int {
        return R.layout.default_state_error_layout
    }

    override fun bindCoverImage(view: ImageView) {
        _onCoverBindListener?.invoke(view)
    }

    override fun onSingleTap() {
        if (_controllerIsVisible) {
            hideController()
        } else {
            showController()
        }
    }

    override fun onDoubleTap() {
        showController()
        controllerListener?.onPlayOrPause(true)
    }

    override fun onLongTap(isTouch: Boolean) {
        if (isTouch) {
            controllerListener?.onTouchSpeed(3.0F, isTouch)
        } else {
            controllerListener?.onTouchSpeed(1.0F, isTouch)
        }
    }

    override fun updatePosition() {
        controllerListener?.onPositionUpdated()
        postDelayed(progressRunnable, _updateInterval)
    }

    override fun stopUpdatePosition() {
        removeCallbacks(progressRunnable)
    }

    override fun setListener(listener: Controller.Listener) {
        if (controllerListener === listener) return
        controllerListener = listener
    }

    override fun isControllerReady(): Boolean = _controllerIsReady

    override fun getView(): View = this

    override fun release() {
        removeCallbacks(progressRunnable)
        removeCallbacks(visibilityRunnable)
    }

    fun setCover(listener: (view: ImageView) -> Unit) {
        this._onCoverBindListener = listener
    }

    fun setGestureController(gestureController: Gesture) {
        if (_gestureController === gestureController) return
        _gestureController = gestureController
    }

    fun setCoverViewEnable(enable: Boolean) {
        _coverViewEnable = enable
    }

    fun setCompletionViewEnable(enable: Boolean) {
        _completionViewEnable = enable
    }

    fun setErrorViewEnable(enable: Boolean) {
        _errorViewEnable = enable
    }

    fun getCoverView(): View = stateCoverView

    fun getCompletionView(): View = stateCompletionView

    fun getErrorView(): View = stateErrorView

    fun setUpdateInterval(millis: Long) {
        _updateInterval = millis
    }

    fun setVisibilityInterval(millis: Long) {
        _visibilityInterval = millis
    }

    fun showController(shouldHide: Boolean = true) {
        onShowController()
        _controllerIsVisible = true
        removeCallbacks(visibilityRunnable)
        if (shouldHide) {
            postDelayed(visibilityRunnable, _visibilityInterval)
        }
    }

    fun hideController() {
        onHideController()
        _controllerIsVisible = false
        removeCallbacks(visibilityRunnable)
    }

    private fun inflateLayout(@LayoutRes layout: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layout, this, attachToRoot)
    }

}