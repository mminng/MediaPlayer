package com.github.mminng.media.controller

import android.content.Context
import android.os.Vibrator
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
import com.github.mminng.media.renderer.RenderMode

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
    private val gestureController: Gesture = GestureController(context, attrs)
    private var _coverBindListener: ((view: ImageView) -> Unit)? = null
    private var _isControllerReadyListener: (() -> Unit)? = null
    private var _listener: Controller.Listener? = null

    private val controllerView: View by lazy {
        inflateLayout(setControllerLayout(), true)
    }
    private val progressView: View by lazy {
        inflateLayout(setSwipeProgressView())
    }
    private val brightnessView: View by lazy {
        inflateLayout(setSwipeBrightnessView())
    }
    private val volumeView: View by lazy {
        inflateLayout(setSwipeVolumeView())
    }
    private val longTouchView: View by lazy {
        inflateLayout(setTouchSpeedView())
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
        addView(gestureController.getView(), 0)
        addView(stateBufferView, 0)
        addView(progressView)
        addView(brightnessView)
        addView(volumeView)
        addView(longTouchView)
        addView(stateCompletionView)
        addView(stateErrorView)
        addView(stateCoverView)
        progressView.visibility = GONE
        brightnessView.visibility = GONE
        volumeView.visibility = GONE
        longTouchView.visibility = GONE
        stateBufferView.visibility = INVISIBLE
        stateCompletionView.visibility = GONE
        stateErrorView.visibility = GONE
        stateCoverView.visibility = if (_coverViewEnable) VISIBLE else GONE
        gestureController.setListener(this)
        _controllerIsReady = true
        _isControllerReadyListener?.invoke()
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
                showController()
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
            }
            PlayerState.PAUSED -> {
            }
            PlayerState.COMPLETION -> {
                if (_completionViewEnable) {
                    stateCompletionView.visibility = VISIBLE
                    hideController()
                } else {
                    showController(false)
                }
                onCompletion()
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

    override fun changeRenderMode(renderMode: RenderMode) {
        _listener?.onChangeRenderMode(renderMode)
    }

    override fun seekTo(position: Int) {
        _listener?.onSeekTo(position)
    }

    override fun playerBack() {
        _listener?.onPlayerBack()
    }

    override fun playOrPause(pauseFromUser: Boolean) {
        _listener?.onPlayOrPause(pauseFromUser)
    }

    override fun screenChanged() {
        _listener?.onScreenChanged()
    }

    override fun changeSpeed(speed: Float) {
        _listener?.onChangeSpeed(speed)
    }

    override fun prepare(playWhenPrepared: Boolean) {
        _listener?.onPrepare(playWhenPrepared)
    }

    override fun replay() {
        _listener?.onReplay()
    }

    override fun retry() {
        _listener?.onRetry()
    }

    override fun setSwipeProgressView(): Int {
        return R.layout.default_swipe_progress_layout
    }

    override fun setSwipeBrightnessView(): Int {
        return R.layout.default_swipe_brightness_layout
    }

    override fun setSwipeVolumeView(): Int {
        return R.layout.default_swipe_volume_layout
    }

    override fun setTouchSpeedView(): Int {
        return R.layout.default_touch_speed_layout
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
        _coverBindListener?.invoke(view)
    }

    override fun onSingleTap() {
        if (_controllerIsVisible) {
            hideController()
        } else {
            showController()
        }
    }

    override fun onDoubleTap() {
        if (_controllerIsVisible) {
            removeCallbacks(visibilityRunnable)
            postDelayed(visibilityRunnable, _visibilityInterval)
        }
        _listener?.onPlayOrPause(true)
    }

    override fun onLongTap(isTouch: Boolean) {
        if (_controllerIsVisible) {
            hideController()
        }
        _listener?.onTouchSpeed(isTouch)
    }

    override fun getCurrentPosition(): Int {
        _listener?.let {
            return it.requireCurrentPosition()
        }
        return 0
    }

    override fun getDuration(): Int {
        _listener?.let {
            return it.requireDuration()
        }
        return 0
    }

    override fun updatePosition() {
        _listener?.onPositionUpdated()
        postDelayed(progressRunnable, _updateInterval)
    }

    override fun stopUpdatePosition() {
        removeCallbacks(progressRunnable)
    }

    override fun setListener(listener: Controller.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

    override fun setControllerReadyListener(listener: () -> Unit) {
        if (_isControllerReadyListener == null) {
            _isControllerReadyListener = listener
        }
    }

    override fun isControllerReady(): Boolean = _controllerIsReady

    override fun onCanBack(): Boolean = true

    override fun getPlayerState(): PlayerState {
        _listener?.requirePlayerState()?.let {
            return it
        }
        return PlayerState.IDLE
    }

    override fun getView(): View = this

    override fun release() {
        removeCallbacks(progressRunnable)
        removeCallbacks(visibilityRunnable)
    }

    fun getSwipeProgressView(): View = progressView

    fun getSwipeBrightnessView(): View = brightnessView

    fun getSwipeVolumeView(): View = volumeView

    fun getTouchSpeedView(): View = longTouchView

    fun getBufferView(): View = stateBufferView

    fun getCoverView(): View = stateCoverView

    fun getCompletionView(): View = stateCompletionView

    fun getErrorView(): View = stateErrorView

    fun setCover(listener: (view: ImageView) -> Unit) {
        this._coverBindListener = listener
    }

    fun setGestureEnable(enable: Boolean) {
        gestureController.setGestureEnable(enable)
    }

    fun getGestureEnable(): Boolean = gestureController.getGestureEnable()

    fun setCoverViewEnable(enable: Boolean) {
        _coverViewEnable = enable
        if (isControllerReady()) {
            if (enable) {
                stateCoverView.visibility = VISIBLE
            } else {
                stateCoverView.visibility = GONE
            }
        }
    }

    fun setCompletionViewEnable(enable: Boolean) {
        _completionViewEnable = enable
        if (isControllerReady()) {
            if (enable) {
                stateCompletionView.visibility = VISIBLE
            } else {
                stateCompletionView.visibility = GONE
            }
        }
    }

    fun setErrorViewEnable(enable: Boolean) {
        _errorViewEnable = enable
        if (isControllerReady()) {
            if (enable) {
                stateErrorView.visibility = VISIBLE
            } else {
                stateErrorView.visibility = GONE
            }
        }
    }

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