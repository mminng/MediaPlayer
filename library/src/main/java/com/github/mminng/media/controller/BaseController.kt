package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.R
import com.github.mminng.media.controller.gesture.Gesture
import com.github.mminng.media.controller.gesture.GestureController
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.utils.d

/**
 * Created by zh on 2021/12/9.
 */
abstract class BaseController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Controller, Gesture.OnGestureListener {

    companion object {
        private var UPDATE_INTERVAL: Long = 500
        private var VISIBILITY_INTERVAL: Long = 5000
    }

    private val progressRunnable: Runnable = Runnable {
        updatePosition()
    }
    private val visibilityRunnable: Runnable = Runnable {
        hideController()
    }

    private var _coverViewEnable: Boolean = false
    private var _completionViewEnable: Boolean = false
    private var _errorViewEnable: Boolean = false
    private var _controllerIsReady: Boolean = false
    private var _gestureController: Gesture = GestureController(context)
    private var _controllerIsVisible: Boolean = false
    private var _onCoverBindListener: ((view: ImageView) -> Unit)? = null
    var controllerListener: Controller.OnControllerListener? = null

    private val controller: View by lazy {
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (_controllerIsReady) return
        onLayoutCreated(controller)
        addView(_gestureController.getView(), 0)
        addView(stateBufferingView, 0)
        addView(stateCompletionView)
        addView(stateErrorView)
        addView(stateCoverView)
        stateBufferingView.visibility = INVISIBLE
        stateCompletionView.visibility = GONE
        stateErrorView.visibility = GONE
        stateCoverView.visibility = if (_coverViewEnable) VISIBLE else GONE
        _gestureController.setOnGestureListener(this)
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

    override fun setBufferingView(): Int {
        return R.layout.default_state_buffering_layout
    }

    override fun setCompletionView(): Int {
        return R.layout.default_state_completion_layout
    }

    override fun setErrorView(): Int {
        return R.layout.default_state_error_layout
    }

    override fun setGestureController(gestureController: Gesture) {
        if (_gestureController === gestureController) return
        _gestureController = gestureController
    }

    override fun bindCoverImage(view: ImageView) {
        _onCoverBindListener?.invoke(view)
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

    override fun onLongPress() {
    }

    override fun updatePosition() {
        controllerListener?.onPositionUpdated()
        postDelayed(progressRunnable, UPDATE_INTERVAL)
    }

    override fun stopUpdatePosition() {
        removeCallbacks(progressRunnable)
    }

    override fun isControllerReady(): Boolean = _controllerIsReady

    override fun getView(): View = this

    override fun setOnControllerListener(listener: Controller.OnControllerListener) {
        if (controllerListener === listener) return
        controllerListener = listener
    }

    override fun release() {
        removeCallbacks(progressRunnable)
        removeCallbacks(visibilityRunnable)
        d("Controller released")
    }

    fun setCover(listener: (view: ImageView) -> Unit) {
        this._onCoverBindListener = listener
    }

    fun getCoverView(): View = stateCoverView

    fun getCompletionView(): View = stateCompletionView

    fun getErrorView(): View = stateErrorView

    fun setUpdateInterval(millis: Long) {
        UPDATE_INTERVAL = millis
    }

    fun setVisibilityInterval(millis: Long) {
        VISIBILITY_INTERVAL = millis
    }

    fun showController(shouldHide: Boolean = true) {
        onShowController()
        _controllerIsVisible = true
        removeCallbacks(visibilityRunnable)
        if (shouldHide) {
//            postDelayed(visibilityRunnable, VISIBILITY_INTERVAL)
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